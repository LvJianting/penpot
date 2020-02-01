;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) 2016-2019 Andrey Antukh <niwi@niwi.nz>

(ns uxbox.emails
  "Main api for send emails."
  (:require
   [clojure.spec.alpha :as s]
   [promesa.core :as p]
   [uxbox.config :as cfg]
   [uxbox.common.exceptions :as ex]
   [uxbox.common.spec :as us]
   [uxbox.db :as db]
   [uxbox.tasks :as tasks]
   [uxbox.media :as media]
   [uxbox.util.emails :as emails]))

;; --- Defaults

(def default-context
  {:static media/resolve-asset
   :comment (constantly nil)})

;; --- Public API

(defn render
  [email context]
  (let [defaults {:from (:email-from cfg/config)
                  :reply-to (:email-reply-to cfg/config)}]
    (email (merge defaults context))))

(defn send!
  "Schedule the email for sending."
  ([email context] (send! db/pool email context))
  ([conn email context]
   (us/verify fn? email)
   (us/verify map? context)
   (let [defaults {:from (:email-from cfg/config)
                   :reply-to (:email-reply-to cfg/config)}
         data (->> (merge defaults context)
                   (email))]
     (tasks/schedule! conn {:name "sendmail"
                            :delay 0
                            :props data}))))

;; --- Emails

(s/def ::name ::us/string)
(s/def ::register
  (s/keys :req-un [::name]))

(def register
  "A new profile registration welcome email."
  (emails/build ::register default-context))

(s/def ::token ::us/string)
(s/def ::password-recovery
  (s/keys :req-un [::name ::token]))

(def password-recovery
  "A password recovery notification email."
  (emails/build ::password-recovery default-context))

