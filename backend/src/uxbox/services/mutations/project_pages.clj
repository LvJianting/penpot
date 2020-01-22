;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) 2019 Andrey Antukh <niwi@niwi.nz>

(ns uxbox.services.mutations.project-pages
  (:require
   [clojure.spec.alpha :as s]
   [promesa.core :as p]
   [uxbox.common.pages :as cp]
   [uxbox.common.exceptions :as ex]
   [uxbox.common.spec :as us]
   [uxbox.db :as db]
   [uxbox.services.mutations :as sm]
   [uxbox.services.mutations.project-files :as files]
   [uxbox.services.queries.project-pages :refer [decode-row]]
   [uxbox.services.util :as su]
   [uxbox.util.blob :as blob]
   [uxbox.util.sql :as sql]
   [uxbox.util.uuid :as uuid]
   [vertx.eventbus :as ve]))

;; --- Helpers & Specs

(s/def ::id ::us/uuid)
(s/def ::name ::us/string)
(s/def ::data ::cp/data)
(s/def ::user ::us/uuid)
(s/def ::project-id ::us/uuid)
(s/def ::ordering ::us/number)

;; --- Mutation: Create Page

(declare create-page)

(s/def ::create-project-page
  (s/keys :req-un [::user ::file-id ::name ::ordering ::data]
          :opt-un [::id]))

(sm/defmutation ::create-project-page
  [{:keys [user file-id] :as params}]
  (db/with-atomic [conn db/pool]
    (files/check-edition-permissions! conn user file-id)
    (create-page conn params)))

(defn create-page
  [conn {:keys [id user file-id name ordering data] :as params}]
  (let [sql "insert into project_pages (id, user_id, file_id, name,
                                        ordering, data, version)
             values ($1, $2, $3, $4, $5, $6, 0)
             returning *"
        id   (or id (uuid/next))
        data (blob/encode data)]
    (-> (db/query-one conn [sql id user file-id name ordering data])
        (p/then' decode-row))))

;; --- Mutation: Update Page Data

(declare select-page-for-update)
(declare update-page-data)
(declare insert-page-snapshot)

(s/def ::update-project-page-data
  (s/keys :req-un [::id ::user ::data]))

(sm/defmutation ::update-project-page-data
  [{:keys [id user data] :as params}]
  (db/with-atomic [conn db/pool]
    (p/let [{:keys [version file-id]} (select-page-for-update conn id)]
      (files/check-edition-permissions! conn user file-id)
      (let [data (blob/encode data)
            version (inc version)
            params (assoc params :id id :data data :version version)]
        (p/do! (update-page-data conn params)
               (insert-page-snapshot conn params)
               (select-keys params [:id :version]))))))

(defn- select-page-for-update
  [conn id]
  (let [sql "select p.id, p.version, p.file_id, p.data
               from project_pages as p
              where p.id = $1
                and deleted_at is null
                 for update;"]
    (-> (db/query-one conn [sql id])
        (p/then' su/raise-not-found-if-nil))))

(defn- update-page-data
  [conn {:keys [id name version data]}]
  (let [sql "update project_pages
                set version = $1,
                    data = $2
              where id = $3"]
    (-> (db/query-one conn [sql version data id])
        (p/then' su/constantly-nil))))

(defn- insert-page-snapshot
  [conn {:keys [user-id id version data changes]}]
  (let [sql "insert into project_page_snapshots (user_id, page_id, version, data, changes)
             values ($1, $2, $3, $4, $5)
             returning id, page_id, user_id, version, changes"]
    (db/query-one conn [sql user-id id version data changes])))

;; --- Mutation: Rename Page

(declare rename-page)

(s/def ::rename-project-page
  (s/keys :req-un [::id ::name ::user]))

(sm/defmutation ::rename-project-page
  [{:keys [id name user]}]
  (db/with-atomic [conn db/pool]
    (p/let [page (select-page-for-update conn id)]
      (files/check-edition-permissions! conn user (:file-id page))
      (rename-page conn (assoc page :name name)))))

(defn- rename-page
  [conn {:keys [id name] :as params}]
  (let [sql "update project_pages
                set name = $2
              where id = $1
                and deleted_at is null"]
    (-> (db/query-one conn [sql id name])
        (p/then su/constantly-nil))))

;; --- Mutation: Update Page

;; A generic, Changes based (granular) page update method.

(s/def ::changes
  (s/coll-of map? :kind vector?))

(s/def ::update-project-page
  (s/keys :opt-un [::id ::user ::version ::changes]))

(declare update-project-page)
(declare retrieve-lagged-changes)

(sm/defmutation ::update-project-page
  [{:keys [id user] :as params}]
  (db/with-atomic [conn db/pool]
    (p/let [{:keys [file-id] :as page} (select-page-for-update conn id)]
      (files/check-edition-permissions! conn user file-id)
      (update-project-page conn page params))))

(defn- update-project-page
  [conn page params]
  (when (> (:version params)
           (:version page))
    (ex/raise :type :validation
              :code :version-conflict
              :hint "The incoming version is greater that stored version."
              :context {:incoming-version (:version params)
                        :stored-version (:version page)}))
  (let [changes (:changes params)
        data (-> (:data page)
                 (blob/decode)
                 (cp/process-changes changes)
                 (blob/encode))

        page (assoc page
                    :user-id (:user params)
                    :data data
                    :version (inc (:version page))
                    :changes (blob/encode changes))]

    (-> (update-page-data conn page)
        (p/then (fn [_] (insert-page-snapshot conn page)))
        (p/then (fn [s]
                  (let [topic (str "internal.uxbox.file." (:file-id page))]
                    (p/do! (ve/publish! uxbox.core/system topic {:type :page-snapshot
                                                                 :user-id (:user-id s)
                                                                 :page-id (:page-id s)
                                                                 :version (:version s)
                                                                 :changes changes})
                           (retrieve-lagged-changes conn s params))))))))

(def sql:lagged-snapshots
  "select s.id, s.changes
     from project_page_snapshots as s
    where s.page_id = $1
      and s.version > $2")

(defn- retrieve-lagged-changes
  [conn snapshot params]
  (let [sql sql:lagged-snapshots]
    (-> (db/query conn [sql (:id params) (:version params) #_(:id snapshot)])
        (p/then (fn [rows]
                  {:page-id (:id params)
                   :version (:version snapshot)
                   :changes (into [] (comp (map decode-row)
                                           (map :changes)
                                           (mapcat identity))
                                  rows)})))))

;; --- Mutation: Delete Page

(declare delete-page)

(s/def ::delete-project-page
  (s/keys :req-un [::user ::id]))

(sm/defmutation ::delete-project-page
  [{:keys [id user]}]
  (db/with-atomic [conn db/pool]
    (p/let [page (select-page-for-update conn id)]
      (files/check-edition-permissions! conn user (:file-id page))
      (delete-page conn id))))

(def sql:delete-page
  "update project_pages
      set deleted_at = clock_timestamp()
    where id = $1
      and deleted_at is null")

(defn- delete-page
  [conn id]
  (let [sql sql:delete-page]
    (-> (db/query-one conn [sql id])
        (p/then su/constantly-nil))))

;; --- Update Page History

;; (defn update-page-history
;;   [conn {:keys [user id label pinned]}]
;;   (let [sqlv (sql/update-page-history {:user user
;;                                        :id id
;;                                        :label label
;;                                        :pinned pinned})]
;;     (some-> (db/fetch-one conn sqlv)
;;             (decode-row))))

;; (s/def ::label ::us/string)
;; (s/def ::update-page-history
;;   (s/keys :req-un [::user ::id ::pinned ::label]))

;; (sm/defmutation :update-page-history
;;   {:doc "Update page history"
;;    :spec ::update-page-history}
;;   [params]
;;   (with-open [conn (db/connection)]
;;     (update-page-history conn params)))
