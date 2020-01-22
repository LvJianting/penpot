;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) 2019 Andrey Antukh <niwi@niwi.nz>

(ns uxbox.services.queries.project-pages
  (:require
   [clojure.spec.alpha :as s]
   [promesa.core :as p]
   [uxbox.common.spec :as us]
   [uxbox.db :as db]
   [uxbox.services.queries :as sq]
   [uxbox.services.util :as su]
   [uxbox.util.blob :as blob]
   [uxbox.util.sql :as sql]))

;; --- Helpers & Specs

(declare decode-row)

(s/def ::id ::us/uuid)
(s/def ::user ::us/uuid)
(s/def ::project-id ::us/uuid)
(s/def ::file-id ::us/uuid)

(def sql:generic-project-pages
  "select pp.*
     from project_pages as pp
    inner join project_files as pf on (pf.id = pp.file_id)
    inner join projects as p on (p.id = pf.project_id)
     left join project_users as pu on (pu.project_id = p.id)
     left join project_file_users as pfu on (pfu.file_id = pf.id)
    where ((pfu.user_id = $1 and pfu.can_edit = true) or
           (pu.user_id = $1 and pu.can_edit = true))
      and pp.deleted_at is null
    order by pp.created_at")

;; --- Query: Project Pages (By File ID)

(def sql:project-pages
  (str "with pages as (" sql:generic-project-pages ")"
       " select * from pages where file_id = $2"))

(s/def ::project-pages
  (s/keys :req-un [::user ::file-id]))

(sq/defquery ::project-pages
  [{:keys [user file-id] :as params}]
  (let [sql sql:project-pages]
    (-> (db/query db/pool [sql user file-id])
        (p/then #(mapv decode-row %)))))

;; --- Query: Project Page (By ID)

(def ^:private sql:project-page
  (str "with pages as (" sql:generic-project-pages ")"
       " select * from pages where id = $2"))

(defn retrieve-page
  [conn {:keys [user id] :as params}]
  (let [sql sql:project-page]
    (-> (db/query-one conn [sql user id])
        (p/then' su/raise-not-found-if-nil)
        (p/then' decode-row))))

(s/def ::project-page
  (s/keys :req-un [::user ::id]))

(sq/defquery ::project-page
  [{:keys [user id] :as params}]
  (retrieve-page db/pool params))

;; --- Query: Project Page History (by Page ID)

;; (def ^:private sql:generic-page-history
;;   "select pph.*
;;      from project_page_history as pph
;;     where pph.page_id = $2
;;       and pph.version < $3
;;     order by pph.version < desc")

;; (def ^:private sql:page-history
;;   (str "with history as (" sql:generic-page-history ")"
;;        " select * from history limit $4"))

;; (def ^:private sql:pinned-page-history
;;   (str "with history as (" sql:generic-page-history ")"
;;        " select * from history where pinned = true limit $4"))

(s/def ::page-id ::us/uuid)
(s/def ::max ::us/integer)
(s/def ::pinned ::us/boolean)
(s/def ::since ::us/integer)

(s/def ::project-page-snapshots
  (s/keys :req-un [::page-id ::user]
          :opt-un [::max ::pinned ::since]))

(defn retrieve-page-snapshots
  [conn {:keys [page-id user since max pinned] :or {since Long/MAX_VALUE max 10}}]
  (let [sql (-> (sql/from ["project_page_snapshots" "ph"])
                (sql/select "ph.*")
                (sql/where ["ph.user_id = ?" user]
                           ["ph.page_id = ?" page-id]
                           ["ph.version < ?" since]
                           (when pinned
                             ["ph.pinned = ?" true]))
                (sql/order "ph.version desc")
                (sql/limit max))]
    (-> (db/query conn (sql/fmt sql))
        (p/then (partial mapv decode-row)))))

(sq/defquery ::project-page-snapshots
  [{:keys [page-id user] :as params}]
  (db/with-atomic [conn db/pool]
    (p/do! (retrieve-page conn {:id page-id :user user})
           (retrieve-page-snapshots conn params))))

;; --- Helpers

(defn decode-row
  [{:keys [data metadata changes] :as row}]
  (when row
    (cond-> row
      data (assoc :data (blob/decode data))
      metadata (assoc :metadata (blob/decode metadata))
      changes (assoc :changes (blob/decode changes)))))
