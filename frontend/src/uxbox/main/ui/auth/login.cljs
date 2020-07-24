;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; This Source Code Form is "Incompatible With Secondary Licenses", as
;; defined by the Mozilla Public License, v. 2.0.
;;
;; Copyright (c) 2020 UXBOX Labs SL

(ns uxbox.main.ui.auth.login
  (:require
   [cljs.spec.alpha :as s]
   [beicon.core :as rx]
   [rumext.alpha :as mf]
   [uxbox.config :as cfg]
   [uxbox.common.spec :as us]
   [uxbox.main.ui.icons :as i]
   [uxbox.main.data.auth :as da]
   [uxbox.main.repo :as rp]
   [uxbox.main.store :as st]
   [uxbox.main.ui.messages :as msgs]
   [uxbox.main.data.messages :as dm]
   [uxbox.main.ui.components.forms :refer [input submit-button form]]
   [uxbox.util.object :as obj]
   [uxbox.util.dom :as dom]
   [uxbox.util.forms :as fm]
   [uxbox.util.i18n :refer [tr t]]
   [uxbox.util.router :as rt]))

(s/def ::email ::us/email)
(s/def ::password ::us/not-empty-string)

(s/def ::login-form
  (s/keys :req-un [::email ::password]))

(defn- login-with-google
  [event]
  (dom/prevent-default event)
  (->> (rp/mutation! :login-with-google {})
       (rx/subs (fn [{:keys [redirect-uri] :as rsp}]
                  (.replace js/location redirect-uri)))))

(mf/defc login-form
  [{:keys [locale] :as props}]
  (let [error? (mf/use-state false)
        submit-event (mf/use-var da/login)

        on-error
        (fn [form event]
          (reset! error? true))

        on-submit
        (fn [form event]
          (reset! error? false)
          (let [params (with-meta (:clean-data form)
                         {:on-error on-error})]
            (st/emit! (@submit-event params))))]

    [:*
     (when @error?
       [:& msgs/inline-banner
        {:type :warning
         :content (t locale "errors.auth.unauthorized")
         :on-close #(reset! error? false)}])

     [:& form {:on-submit on-submit
               :spec ::login-form
               :initial {}}
      [:& input
       {:name :email
        :type "text"
        :tab-index "2"
        :help-icon i/at
        :label (t locale "auth.email-label")}]
      [:& input
       {:type "password"
        :name :password
        :tab-index "3"
        :help-icon i/eye
        :label (t locale "auth.password-label")}]
      [:& submit-button
       {:label (t locale "auth.login-submit-label")
        :on-click #(reset! submit-event da/login)}]
      (when cfg/login-with-ldap
        [:& submit-button
         {:label (t locale "auth.login-with-ldap-submit-label")
          :on-click #(reset! submit-event da/login-with-ldap)}])]]))

(mf/defc login-page
  [{:keys [locale] :as props}]

  [:div.generic-form.login-form
   [:div.form-container
    [:h1 (t locale "auth.login-title")]
    [:div.subtitle (t locale "auth.login-subtitle")]

    [:& login-form {:locale locale}]

    [:div.links
     [:div.link-entry
      [:a {:on-click #(st/emit! (rt/nav :auth-recovery-request))
           :tab-index "5"}
       (t locale "auth.forgot-password")]]

     [:div.link-entry
      [:span (t locale "auth.register-label") " "]
      [:a {:on-click #(st/emit! (rt/nav :auth-register))
           :tab-index "6"}
       (t locale "auth.register")]]]

    (when cfg/google-client-id
      [:a.btn-ocean.btn-large.btn-google-auth
       {:on-click login-with-google}
       "Login with Google"])

    [:div.links.demo
     [:div.link-entry
      [:span (t locale "auth.create-demo-profile-label") " "]
      [:a {:on-click #(st/emit! da/create-demo-profile)
           :tab-index "6"}
       (t locale "auth.create-demo-profile")]]]]])
