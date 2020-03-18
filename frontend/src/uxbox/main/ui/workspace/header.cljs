;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; This Source Code Form is "Incompatible With Secondary Licenses", as
;; defined by the Mozilla Public License, v. 2.0.
;;
;; Copyright (c) 2015-2017 Andrey Antukh <niwi@niwi.nz>
;; Copyright (c) 2015-2017 Juan de la Cruz <delacruzgarciajuan@gmail.com>

(ns uxbox.main.ui.workspace.header
  (:require
   [lentes.core :as l]
   [rumext.alpha :as mf]
   [uxbox.builtins.icons :as i :include-macros true]
   [uxbox.config :as cfg]
   [uxbox.main.data.history :as udh]
   [uxbox.main.data.workspace :as dw]
   [uxbox.main.refs :as refs]
   [uxbox.main.store :as st]
   [uxbox.main.ui.modal :as modal]
   [uxbox.main.ui.workspace.images :refer [import-image-modal]]
   [uxbox.main.ui.components.dropdown :refer [dropdown]]
   [uxbox.util.i18n :as i18n :refer [tr t]]
   [uxbox.util.math :as mth]
   [uxbox.util.router :as rt]))

;; --- Zoom Widget

(mf/defc zoom-widget
  {:wrap [mf/wrap-memo]}
  [props]
  (let [zoom (mf/deref refs/selected-zoom)
        increase #(st/emit! dw/increase-zoom)
        decrease #(st/emit! dw/decrease-zoom)]
    [:div.zoom-input
     [:span.add-zoom {:on-click decrease} "-"]
     [:span {} (str (mth/round (* 100 zoom)) "%")]
     [:span.remove-zoom {:on-click increase} "+"]]))

;; --- Header Users

(mf/defc user-widget
  [{:keys [user self?] :as props}]
  [:li.tooltip.tooltip-bottom
   {:alt (:fullname user)
    :on-click (when self?
                #(st/emit! (rt/navigate :settings/profile)))}
   [:img {:style {:border-color (:color user)}
          :src (if self? "/images/avatar.jpg" "/images/avatar-red.jpg")}]])

(mf/defc active-users
  [props]
  (let [profile (mf/deref refs/profile)
        users (mf/deref refs/workspace-users)]
    [:ul.user-multi
     [:& user-widget {:user profile :self? true}]
     (for [id (->> (:active users)
                   (remove #(= % (:id profile))))]
       [:& user-widget {:user (get-in users [:by-id id])
                        :key id}])]))

;; --- Header Component

(mf/defc header
  [{:keys [page file layout] :as props}]
  (let [toggle-layout #(st/emit! (dw/toggle-layout-flag %))
        on-undo (constantly nil)
        on-redo (constantly nil)
        locale (i18n/use-locale)

        show-menu? (mf/use-state false)

        on-image #(modal/show! import-image-modal {})
        ;;on-download #(udl/open! :download)
        selected-drawtool (mf/deref refs/selected-drawing-tool)
        select-drawtool #(st/emit! :interrupt
                                   #_(dw/deactivate-ruler)
                                   (dw/select-for-drawing %))]

    [:header.workspace-bar
     [:div.main-icon
      [:a {:on-click #(st/emit! (rt/nav :dashboard-team {:team-id "self"}))}
       i/logo-icon]]

     [:div.menu-btn {:on-click #(reset! show-menu? true)} i/actions]

     [:& dropdown {:show @show-menu?
                   :on-close #(reset! show-menu? false)}
      [:ul.workspace-menu
       [:li i/user [:span (t locale "dashboard.header.profile-menu.profile")]]
       [:li i/lock [:span (t locale "dashboard.header.profile-menu.password")]]
       [:li i/exit [:span (t locale "dashboard.header.profile-menu.logout")]]]]

     [:div.project-tree-btn
      {:alt (tr "header.sitemap")
       :class (when (contains? layout :sitemap) "selected")
       :on-click #(st/emit! (dw/toggle-layout-flag :sitemap))}
      [:span.project-name "Project name /"]
      [:span (:name file)]]

     [:div.workspace-options
      [:& active-users]]
     [:& zoom-widget]]))
