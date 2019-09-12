;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) 2015-2016 Andrey Antukh <niwi@niwi.nz>
;; Copyright (c) 2015-2016 Juan de la Cruz <delacruzgarciajuan@gmail.com>

(ns uxbox.main.ui.workspace.sidebar.icons
  (:require
   [lentes.core :as l]
   [rumext.core :as mx]
   [uxbox.builtins.icons :as i]
   [uxbox.main.data.icons :as udi]
   [uxbox.main.data.workspace :as dw]
   [uxbox.main.store :as st]
   [uxbox.main.ui.dashboard.icons :as icons]
   [uxbox.main.ui.shapes.icon :as icon]
   [uxbox.util.data :refer (read-string)]
   [uxbox.util.dom :as dom]
   [uxbox.util.router :as r]))

;; --- Refs

;; (def ^:private drawing-shape-ref
;;   "A focused vision of the drawing property
;;   of the workspace status. This avoids
;;   rerender the whole toolbox on each workspace
;;   change."
;;   (l/derive ul/selected-drawing st/state))

(def ^:private icons-toolbox-ref
  (-> (l/in [:workspace :icons-toolbox])
      (l/derive st/state)))

;; --- Icons (Component)

(mx/defc icon-wrapper
  {:mixins [mx/static]}
  [icon]
  (icon/icon-svg icon))

(defn- icons-toolbox-init
  [own]
  (st/emit! (dw/initialize-icons-toolbox))
  own)

(mx/defc icons-toolbox
  {:mixins [mx/static mx/reactive]
   :init icons-toolbox-init}
  []
  #_(let [drawing (mx/react drawing-shape-ref)
        selected (mx/react icons-toolbox-ref)
        colls (mx/react icons/collections-ref)
        selected-coll (get colls selected)

        colls (->> (vals (mx/react icons/collections-ref))
                   (sort-by :name))
        icons (->> (vals (mx/react icons/icons-ref))
                   (filter #(= (:id selected-coll) (:collection %))))]
    (letfn [(on-close [event]
              (st/emit! (dw/toggle-flag :icons)))
            (on-select [icon event]
              (st/emit! (dw/select-for-drawing icon)))
            (on-change [event]
              (let [value (read-string (dom/event->value event))]
                (st/emit! (dw/select-for-drawing nil)
                          (dw/select-icons-toolbox-collection value))))]
      [:div#form-figures.tool-window
       [:div.tool-window-bar
        [:div.tool-window-icon i/icon-set]
        [:span "Icons"]
        [:div.tool-window-close {:on-click on-close} i/close]]
       [:div.tool-window-content
        [:div.figures-catalog
         ;; extract component: set selector
         [:select.input-select.small {:on-change on-change
                                      :value (pr-str (:id selected-coll))}
          [:option {:value (pr-str nil)} "Storage"]
          (for [coll colls]
            [:option {:key (str "icon-coll" (:id coll))
                      :value (pr-str (:id coll))}
             (:name coll)])]]
        (for [icon icons
              :let [selected? (= drawing icon)]]
          [:div.figure-btn {:key (str (:id icon))
                            :class (when selected? "selected")
                            :on-click (partial on-select icon)}
           (icon-wrapper icon)])]])))
