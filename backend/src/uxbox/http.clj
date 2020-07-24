;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; This Source Code Form is "Incompatible With Secondary Licenses", as
;; defined by the Mozilla Public License, v. 2.0.
;;
;; Copyright (c) 2020 UXBOX Labs SL

(ns uxbox.http
  (:require
   [clojure.tools.logging :as log]
   [mount.core :as mount :refer [defstate]]
   [reitit.ring :as rring]
   [ring.adapter.jetty9 :as jetty]
   [uxbox.config :as cfg]
   [uxbox.http.debug :as debug]
   [uxbox.http.errors :as errors]
   [uxbox.http.handlers :as handlers]
   [uxbox.http.auth :as auth]
   [uxbox.http.auth.google :as google]
   [uxbox.http.auth.ldap :as ldap]
   [uxbox.http.middleware :as middleware]
   [uxbox.http.session :as session]
   [uxbox.http.ws :as ws]
   [uxbox.metrics :as mtx]
   [uxbox.services.notifications :as usn]))

(defn- create-router
  []
  (rring/router
   [["/metrics" {:get mtx/dump}]
    ["/api" {:middleware [[middleware/format-response-body]
                          [middleware/errors errors/handle]
                          [middleware/parse-request-body]
                          [middleware/params]
                          [middleware/multipart-params]
                          [middleware/keyword-params]
                          [middleware/cookies]]}

     ["/oauth"
      ["/google" {:post google/auth}]
      ["/google/callback" {:get google/callback}]]

     ["/echo" {:get handlers/echo-handler
               :post handlers/echo-handler}]

     ["/login" {:handler auth/login-handler
                :method :post}]
     ["/logout" {:handler auth/logout-handler
                 :method :post}]
     ["/login-ldap" {:handler ldap/auth
                     :method :post}]

     ["/w" {:middleware [session/auth]}
      ["/query/:type" {:get handlers/query-handler}]
      ["/mutation/:type" {:post handlers/mutation-handler}]]]]))

(defstate app
  :start (rring/ring-handler
          (create-router)
          (constantly {:status 404, :body ""})
          {:middleware [[middleware/development-resources]
                        [middleware/development-cors]
                        [middleware/metrics]]}))

(defn start-server
  [cfg app]
  (let [wsockets {"/ws/notifications" ws/handler}
        options  {:port (:http-server-port cfg)
                  :h2c? true
                  :join? false
                  :allow-null-path-info true
                  :websockets wsockets}]
    (jetty/run-jetty app options)))

(defstate server
  :start (start-server cfg/config app)
  :stop (.stop server))
