;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; This Source Code Form is "Incompatible With Secondary Licenses", as
;; defined by the Mozilla Public License, v. 2.0.
;;
;; Copyright (c) 2020 UXBOX Labs SL

(ns app.util.i18n
  "A i18n foundation."
  (:require
   [beicon.core :as rx]
   [cuerdas.core :as str]
   [goog.object :as gobj]
   [okulary.core :as l]
   [rumext.alpha :as mf]
   [app.config :as cfg]
   [app.util.storage :refer [storage]]
   [app.util.transit :as t]))

(def supported-locales
  [{:label "English" :value "en"}
   {:label "Español" :value "es"}
   {:label "Français (community)" :value "fr"}
   {:label "Deutsche (community)" :value "de"}
   {:label "Русский (community)" :value "ru"}
   {:label "简体中文 (community)" :value "zh_cn"}])

(defn- parse-locale
  [locale]
  (let [locale (-> (.-language js/navigator)
                   (str/lower)
                   (str/replace "-" "_"))]
    (cond-> [locale]
      (str/includes? locale "_")
      (conj (subs locale 0 2)))))

(def ^:private browser-locales
  (delay
    (-> (.-language js/navigator)
        (parse-locale))))

(defn- autodetect
  []
  (let [supported (into #{} (map :value supported-locales))]
    (loop [locales (seq @browser-locales)]
      (if-let [locale (first locales)]
        (if (contains? supported locale)
          locale
          (recur (rest locales)))
        cfg/default-language))))

(defonce translations #js {})
(defonce locale (l/atom (or (get storage ::locale)
                            (autodetect))))

;; The traslations `data` is a javascript object and should be treated
;; with `goog.object` namespace functions instead of a standart
;; clojure functions. This is for performance reasons because this
;; code is executed in the critical part (application bootstrap) and
;; used in many parts of the application.

(defn init!
  [data]
  (set! translations data))

(defn set-locale!
  [lang]
  (if lang
    (do
      (swap! storage assoc ::locale lang)
      (reset! locale lang))
    (do
      (reset! locale (autodetect)))))

(defn reset-locale
  "Set the current locale to the browser detected one if it is
  supported or default locale if not."
  []
  (swap! storage dissoc ::locale)
  (reset! locale (autodetect)))

(deftype C [val]
  IDeref
  (-deref [o] val))

(defn ^boolean c?
  [r]
  (instance? C r))

;; A main public api for translate strings.

;; A marker type that is used just for mark
;; a parameter that reprsentes the counter.

(defn c
  [x]
  (C. x))

(defn empty-string?
  [v]
  (or (nil? v) (empty? v)))

(defn t
  ([locale code]
   (let [code  (name code)
         value (gobj/getValueByKeys translations code locale)]
     (if (empty-string? value)
       (if (= cfg/default-language locale)
         code
         (t cfg/default-language code))
       (if (array? value)
         (aget value 0)
         value))))
  ([locale code & args]
   (let [code   (name code)
         value  (gobj/getValueByKeys translations code locale)]
     (if (empty-string? value)
       (if (= cfg/default-language locale)
         code
         (apply t cfg/default-language code args))
       (let [plural (first (filter c? args))
             value  (if (array? value)
                      (if (= @plural 1) (aget value 0) (aget value 1))
                      value)]
         (apply str/fmt value (map #(if (c? %) @% %) args)))))))

(defn tr
  ([code] (t @locale code))
  ([code & args] (apply t @locale code args)))

;; DEPRECATED
(defn use-locale
  []
  (mf/deref locale))

