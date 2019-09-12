;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) 2015-2017 Juan de la Cruz <delacruzgarciajuan@gmail.com>
;; Copyright (c) 2015-2019 Andrey Antukh <niwi@niwi.nz>

(ns uxbox.main.ui.auth.recovery
  (:require
   [cljs.spec.alpha :as s]
   [cuerdas.core :as str]
   [lentes.core :as l]
   [rumext.core :as mx :include-macros true]
   [uxbox.builtins.icons :as i]
   [uxbox.main.data.auth :as uda]
   [uxbox.main.store :as st]
   [uxbox.main.ui.messages :refer [messages-widget]]
   [uxbox.main.ui.navigation :as nav]
   [uxbox.util.dom :as dom]
   [uxbox.util.forms :as fm]
   [uxbox.util.i18n :refer (tr)]
   [uxbox.util.router :as rt]))

;; (def form-data (fm/focus-data :recovery st/state))
;; (def form-errors (fm/focus-errors :recovery st/state))

;; (def assoc-value (partial fm/assoc-value :recovery))
;; (def assoc-errors (partial fm/assoc-errors :recovery))
;; (def clear-form (partial fm/clear-form :recovery))

;; ;; --- Recovery Form

;; (s/def ::password ::fm/non-empty-string)
;; (s/def ::recovery-form
;;   (s/keys :req-un [::password]))

;; (mx/defc recovery-form
;;   {:mixins [mx/static mx/reactive]}
;;   [token]
;;   (let [data (merge (mx/react form-data) {:token token})
;;         valid? (fm/valid? ::recovery-form data)]
;;     (letfn [(on-change [field event]
;;               (let [value (dom/event->value event)]
;;                 (st/emit! (assoc-value field value))))
;;             (on-submit [event]
;;               (dom/prevent-default event)
;;               (st/emit! (uda/recovery data)
;;                         (clear-form)))]
;;       [:form {:on-submit on-submit}
;;        [:div.login-content
;;         [:input.input-text
;;          {:name "password"
;;           :value (:password data "")
;;           :on-change (partial on-change :password)
;;           :placeholder (tr "recover.password.placeholder")
;;           :type "password"}]
;;         [:input.btn-primary
;;          {:name "login"
;;           :class (when-not valid? "btn-disabled")
;;           :disabled (not valid?)
;;           :value (tr "recover.recover-password")
;;           :type "submit"}]
;;         [:div.login-links
;;          [:a {:on-click #(st/emit! (rt/navigate :auth/login))} (tr "recover.go-back")]]]])))

;; ;; --- Recovery Page

;; (defn- recovery-page-init
;;   [own]
;;   (let [[token] (::mx/args own)]
;;     (st/emit! (uda/validate-recovery-token token))
;;     own))

;; (mx/defc recovery-page
;;   {:mixins [mx/static (fm/clear-mixin st/store :recovery)]
;;    :init recovery-page-init}
;;   [token]
;;   [:div.login
;;    [:div.login-body
;;     (messages-widget)
;;     [:a i/logo]
;;     (recovery-form token)]])
