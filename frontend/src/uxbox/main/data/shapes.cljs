;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) 2015-2019 Andrey Antukh <niwi@niwi.nz>

(ns uxbox.main.data.shapes
  (:require
   [cljs.spec.alpha :as s]
   [uxbox.main.geom :as geom]
   [uxbox.util.data :refer [index-of]]
   [uxbox.util.geom.matrix :as gmt]
   [uxbox.util.spec :as us]
   [uxbox.util.uuid :as uuid]))

;; --- Specs

(s/def ::id ::us/uuid)
(s/def ::blocked boolean?)
(s/def ::collapsed boolean?)
(s/def ::content string?)
(s/def ::fill-color string?)
(s/def ::fill-opacity number?)
(s/def ::font-family string?)
(s/def ::font-size number?)
(s/def ::font-style string?)
(s/def ::font-weight string?)
(s/def ::height number?)
(s/def ::hidden boolean?)
(s/def ::id uuid?)
(s/def ::letter-spacing number?)
(s/def ::line-height number?)
(s/def ::locked boolean?)
(s/def ::name string?)
(s/def ::page uuid?)
(s/def ::proportion number?)
(s/def ::proportion-lock boolean?)
(s/def ::rx number?)
(s/def ::ry number?)
(s/def ::stroke-color string?)
(s/def ::stroke-opacity number?)
(s/def ::stroke-style #{:none :solid :dotted :dashed :mixed})
(s/def ::stroke-width number?)
(s/def ::text-align #{"left" "right" "center" "justify"})
(s/def ::type #{:rect :path :circle :image :text})
(s/def ::width number?)
(s/def ::x1 number?)
(s/def ::x2 number?)
(s/def ::y1 number?)
(s/def ::y2 number?)

(s/def ::attributes
  (s/keys :opt-un [::blocked
                   ::collapsed
                   ::content
                   ::fill-color
                   ::fill-opacity
                   ::font-family
                   ::font-size
                   ::font-style
                   ::font-weight
                   ::hidden
                   ::letter-spacing
                   ::line-height
                   ::locked
                   ::proportion
                   ::proportion-lock
                   ::rx ::ry
                   ::stroke-color
                   ::stroke-opacity
                   ::stroke-style
                   ::stroke-width
                   ::text-align
                   ::x1 ::x2
                   ::y1 ::y2]))

(s/def ::minimal-shape
  (s/keys :req-un [::id ::page ::type ::name]))

(s/def ::shape
  (s/and ::minimal-shape ::attributes))

(s/def ::rect-like-shape
  (s/keys :req-un [::x1 ::y1 ::x2 ::y2 ::type]))

;; --- Shape Creation

(defn retrieve-used-names
  "Returns a set of already used names by shapes
  in the current page."
  [{:keys [shapes] :as state}]
  (let [pid (get-in state [:workspace :current])
        xf  (comp (filter #(= pid (:page %)))
                  (map :name))]
    (into #{} xf (vals shapes))))

(defn generate-unique-name
  "A unique name generator based on the previous
  state of the used names."
  [state basename]
  (let [used (retrieve-used-names state)]
    (loop [counter 1]
      (let [candidate (str basename "-" counter)]
        (if (contains? used candidate)
          (recur (inc counter))
          candidate)))))

(defn assoc-shape-to-page
  [state shape page]
  (let [shape-id (uuid/random)
        shape-name (generate-unique-name state (:name shape))
        shape (assoc shape
                     :page page
                     :id shape-id
                     :name shape-name)]
    (-> state
        (update-in [:pages page :shapes] #(into [] (cons shape-id %)))
        (assoc-in [:shapes shape-id] shape))))

(defn duplicate-shapes'
  ([state shapes page]
   (duplicate-shapes' state shapes page nil))
  ([state shapes page group]
   (letfn [(duplicate-shape [state shape page group]
             (if (= (:type shape) :group)
               (let [id (uuid/random)
                     items (:items shape)
                     name (generate-unique-name state (str (:name shape) "-copy"))
                     shape (assoc shape
                                  :id id
                                  :page page
                                  :items []
                                  :name name)
                     state (if (nil? group)
                             (-> state
                                 (update-in [:pages page :shapes]
                                            #(into [] (cons id %)))
                                 (assoc-in [:shapes id] shape))
                             (-> state
                                 (update-in [:shapes group :items]
                                            #(into [] (cons id %)))
                                 (assoc-in [:shapes id] shape)))]
                 (->> (map #(get-in state [:shapes %]) items)
                      (reverse)
                      (reduce #(duplicate-shape %1 %2 page id) state)))
               (let [id (uuid/random)
                     name (generate-unique-name state (str (:name shape) "-copy"))
                     shape (-> (dissoc shape :group)
                               (assoc :id id :page page :name name)
                               (merge (when group {:group group})))]
                 (if (nil? group)
                   (-> state
                       (update-in [:pages page :shapes] #(into [] (cons id %)))
                       (assoc-in [:shapes id] shape))
                   (-> state
                       (update-in [:shapes group :items] #(into [] (cons id %)))
                       (assoc-in [:shapes id] shape))))))]
     (reduce #(duplicate-shape %1 %2 page group) state shapes))))

(defn duplicate-shapes
  ([state shapes]
   (duplicate-shapes state shapes nil))
  ([state shapes page]
   (letfn [(all-toplevel? [coll]
             (every? #(nil? (:group %)) coll))
           (all-same-group? [coll]
             (let [group (:group (first coll))]
               (every? #(= group (:group %)) coll)))]
     (let [shapes (reverse (mapv #(get-in state [:shapes %]) shapes))]
       (cond
         (all-toplevel? shapes)
         (let [page (or page (:page (first shapes)))]
           (duplicate-shapes' state shapes page))

         (all-same-group? shapes)
         (let [page (or page (:page (first shapes)))
               group (:group (first shapes))]
           (duplicate-shapes' state shapes page group))

         :else
         (let [page (or page (:page (first shapes)))]
           (duplicate-shapes' state shapes page)))))))

;; --- Delete Shapes

(defn dissoc-from-index
  "A function that dissoc shape from the indexed
  data structure of shapes from the state."
  [state {:keys [id type] :as shape}]
  (if (= :group type)
    (let [items (map #(get-in state [:shapes %]) (:items shape))]
      (as-> state $
        (update-in $ [:shapes] dissoc id)
        (reduce dissoc-from-index $ items)))
    (update-in state [:shapes] dissoc id)))

(defn dissoc-from-page
  "Given a shape, try to remove its reference from the
  corresponding page."
  [state {:keys [id page] :as shape}]
  (as-> (get-in state [:pages page :shapes]) $
    (into [] (remove #(= % id) $))
    (assoc-in state [:pages page :shapes] $)))

(defn dissoc-from-group
  "Given a shape, try to remove its reference from the
  corresponding group (only if it belongs to one group)."
  [state {:keys [id group] :as shape}]
  (if-let [group' (get-in state [:shapes group])]
    (as-> (:items group') $
      (into [] (remove #(= % id) $))
      (assoc-in state [:shapes group :items] $))
    state))

(declare dissoc-shape)

(defn clear-empty-groups
  "Given the shape, try to clean all empty groups
  that this shape belongs to.

  The main purpose of this function is remove the
  all empty parent groups of recently removed
  shape."
  [state {:keys [group] :as shape}]
  (if-let [group' (get-in state [:shapes group])]
    (if (empty? (:items group'))
      (-> (dissoc-shape state group')
          (update-in [:workspace :selected] disj (:id group')))
      state)
    state))

(defn dissoc-shape
  "Given a shape, removes it from the state."
  [state shape]
  (as-> state $
    (dissoc-from-page $ shape)
    (dissoc-from-group $ shape)
    (dissoc-from-index $ shape)
    (clear-empty-groups $ shape)))

;; --- Shape Movements

(defn- drop-at-index
  [index coll v]
  (let [[fst snd] (split-at index coll)]
    (into [] (concat fst [v] snd))))

(defn drop-relative
  [state loc sid]
  {:pre [(not (nil? sid))]}
  (let [shape (get-in state [:shapes (first sid)])
        {:keys [page group]} shape
        sid (:id shape)

        shapes (if group
                 (get-in state [:shapes group :items])
                 (get-in state [:pages page :shapes]))

        index (case loc
                :first 0
                :after (min (- (count shapes) 1) (inc (index-of shapes sid)))
                :before (max 0 (- (index-of shapes sid) 1))
                :last (- (count shapes) 1))

        state (-> state
                  (dissoc-from-page shape)
                  (dissoc-from-group shape))

        shapes (if group
                 (get-in state [:shapes group :items])
                 (get-in state [:pages page :shapes]))

        shapes (drop-at-index index shapes sid)]

    (if group
      (as-> state $
        (assoc-in $ [:shapes group :items] shapes)
        (update-in $ [:shapes sid] assoc :group group)
        (clear-empty-groups $ shape))
      (as-> state $
        (assoc-in $ [:pages page :shapes] shapes)
        (update-in $ [:shapes sid] dissoc :group)
        (clear-empty-groups $ shape)))))

(defn drop-aside
  [state loc tid sid]
  {:pre [(not= tid sid)
         (not (nil? tid))
         (not (nil? sid))]}
  (let [{:keys [page group]} (get-in state [:shapes tid])
        source (get-in state [:shapes sid])

        state (-> state
                  (dissoc-from-page source)
                  (dissoc-from-group source))

        shapes (if group
                 (get-in state [:shapes group :items])
                 (get-in state [:pages page :shapes]))

        index (case loc
                :after (inc (index-of shapes tid))
                :before (index-of shapes tid))

        shapes (drop-at-index index shapes sid)]
    (if group
      (as-> state $
        (assoc-in $ [:shapes group :items] shapes)
        (update-in $ [:shapes sid] assoc :group group)
        (clear-empty-groups $ source))
      (as-> state $
        (assoc-in $ [:pages page :shapes] shapes)
        (update-in $ [:shapes sid] dissoc :group)
        (clear-empty-groups $ source)))))

(def drop-after #(drop-aside %1 :after %2 %3))
(def drop-before #(drop-aside %1 :before %2 %3))

(defn drop-inside
  [state tid sid]
  {:pre [(not= tid sid)]}
  (let [source (get-in state [:shapes sid])
        state (-> state
                  (dissoc-from-page source)
                  (dissoc-from-group source))
        shapes (get-in state [:shapes tid :items])]
    (if (seq shapes)
      (as-> state $
        (assoc-in $ [:shapes tid :items] (conj shapes sid))
        (update-in $ [:shapes sid] assoc :group tid))
      state)))

(defn drop-shape
  [state sid tid loc]
  (if (= tid sid)
    state
    (case loc
      :inside (drop-inside state tid sid)
      :before (drop-before state tid sid)
      :after (drop-after state tid sid)
      (throw (ex-info "Invalid data" {})))))

(defn move-layer
  [state shape loc]
  (case loc
    :up (drop-relative state :before shape)
    :down (drop-relative state :after shape)
    :top (drop-relative state :first shape)
    :bottom (drop-relative state :last shape)
    (throw (ex-info "Invalid data" {}))))

;; --- Shape Selection

(defn- try-match-shape
  [xf selrect acc {:keys [type id items] :as shape}]
  (cond
    (geom/contained-in? shape selrect)
    (conj acc id)

    (geom/overlaps? shape selrect)
    (conj acc id)

    (:locked shape)
    acc

    (= type :group)
    (reduce (partial try-match-shape xf selrect)
            acc (sequence xf items))

    :else
    acc))

(defn match-by-selrect
  [state page-id selrect]
  (let [xf (comp (map #(get-in state [:shapes %]))
                 (remove :hidden)
                 (remove :blocked)
                 (remove #(= :canvas (:type %)))
                 (map geom/selection-rect))
        match (partial try-match-shape xf selrect)
        shapes (get-in state [:pages page-id :shapes])]
    (reduce match #{} (sequence xf shapes))))

(defn group-shapes
  [state shapes page]
  (letfn [(replace-first-item [pred coll replacement]
            (into []
              (concat
                (take-while #(not (pred %)) coll)
                [replacement]
                (drop 1 (drop-while #(not (pred %)) coll)))))

          (move-shapes-to-new-group [state page shapes new-group]
            (reduce (fn [state {:keys [id group] :as shape}]
                      (-> state
                        (update-in [:shapes group :items] #(remove (set [id]) %))
                        (update-in [:pages page :shapes] #(remove (set [id]) %))
                        (clear-empty-groups shape)
                        (assoc-in [:shapes id :group] new-group)
                        ))
                    state
                    shapes))

          (update-shapes-on-page [state page shapes group]
            (as-> (get-in state [:pages page :shapes]) $
              (replace-first-item (set shapes) $ group)
              (remove (set shapes) $)
              (into [] $)
              (assoc-in state [:pages page :shapes] $)))

          (update-shapes-on-group [state parent-group shapes group]
            (as-> (get-in state [:shapes parent-group :items]) $
              (replace-first-item (set shapes) $ group)
              (remove (set shapes) $)
              (into [] $)
              (assoc-in state [:shapes parent-group :items] $)))

          (update-shapes-on-index [state shapes group]
            (reduce (fn [state {:keys [id] :as shape}]
                      (as-> shape $
                        (assoc $ :group group)
                        (assoc-in state [:shapes id] $)))
                    state
                    shapes))]
    (let [sid (uuid/random)
          shapes' (map #(get-in state [:shapes %]) shapes)
          distinct-groups (distinct (map :group shapes'))
          parent-group (cond
                         (not= 1 (count distinct-groups)) :multi
                         (nil? (first distinct-groups)) :page
                         :else (first distinct-groups))
          name (generate-unique-name state "Group")
          group {:type :group
                 :name name
                 :items (into [] shapes)
                 :id sid
                 :page page}]
      (as-> state $
        (update-shapes-on-index $ shapes' sid)
        (cond
          (= :multi parent-group)
            (-> $
              (move-shapes-to-new-group page shapes' sid)
              (update-in [:pages page :shapes] #(into [] (cons sid %))))
          (= :page parent-group)
            (update-shapes-on-page $ page shapes sid)
          :else
            (update-shapes-on-group $ parent-group shapes sid))
        (update $ :shapes assoc sid group)
        (cond
          (= :multi parent-group) $
          (= :page parent-group) $
          :else (assoc-in $ [:shapes sid :group] parent-group))
        (update $ :workspace assoc :selected #{sid})))))

(defn degroup-shapes
  [state shapes page-id]
  (letfn [(get-relocation-position [state {id :id parent-id :group}]
            (if (nil? parent-id)
              (index-of (get-in state [:pages page-id :shapes]) id)
              (index-of (get-in state [:shapes parent-id :items]) id)))

          (relocate-shape [state shape-id parent-id position]
            (if (nil? parent-id)
              (-> state
                  (update-in [:pages page-id :shapes] #(drop-at-index position % shape-id))
                  (update-in [:shapes shape-id] dissoc :group))
              (-> state
                  (update-in [:shapes parent-id :items] #(drop-at-index position % shape-id))
                  (assoc-in [:shapes shape-id :group] parent-id))))

          (remove-group [state {id :id parent-id :group}]
            (let [xform (remove #{id})]
              (as-> state $
                (update $ :shapes dissoc id)
                (if (nil? parent-id)
                  (update-in $ [:pages page-id :shapes] #(into [] xform %))
                  (update-in $ [:shapes parent-id :items] #(into [] xform %))))))

          (relocate-group-items [state {id :id parent-id :group items :items :as group}]
            (let [position (get-relocation-position state group)]
              (as-> state $
                (reduce #(relocate-shape %1 %2 parent-id position) $ (reverse items))
                (remove-group $ group))))

          (select-degrouped [state groups]
            (let [items (into #{} (mapcat :items groups))]
              (assoc-in state [:workspace :selected] items)))

          (remove-from-parent [state id parent-id]
            (assert (not (nil? parent-id)) "parent-id should never be nil here")
            (update-in state [:shapes parent-id :items] #(into [] (remove #{id}) %)))

          (strip-empty-groups [state parent-id]
            (if (nil? parent-id)
              state
              (let [group (get-in state [:shapes parent-id])]
                (if (empty? (:items group))
                  (-> state
                      (remove-group group)
                      (strip-empty-groups (:group group)))
                  state))))

          (selective-degroup [state [shape & rest :as shapes]]
            (let [group (get-in state [:shapes (:group shape)])
                  position (get-relocation-position state group)
                  parent-id (:group group)]
              (as-> state $
                (assoc-in $ [:workspace :selected] (into #{} (map :id shapes)))
                (reduce (fn [state {shape-id :id}]
                          (-> state
                              (relocate-shape shape-id parent-id position)
                              (remove-from-parent shape-id (:id group))))
                        $ (reverse shapes))
                (strip-empty-groups $ (:id group)))))]

    (let [shapes (into #{} (map #(get-in state [:shapes %])) shapes)
          groups (into #{} (filter #(= (:type %) :group)) shapes)
          parents (into #{} (map :group) shapes)]
      (cond
        (and (= (count shapes) (count groups))
             (= 1 (count parents))
             (not (empty? groups)))
        (as-> state $
          (reduce relocate-group-items $ groups)
          (reduce remove-group $ groups)
          (select-degrouped $ groups))

        (and (empty? groups)
             (= 1 (count parents))
             (not (nil? (first parents))))
        (selective-degroup state shapes)

        :else
        (throw (ex-info "invalid condition for degrouping" {}))))))

(defn materialize-xfmt
  [state id xfmt]
  (let [{:keys [type items] :as shape} (get-in state [:shapes id])]
   (if (= type :group)
      (reduce #(materialize-xfmt %1 %2 xfmt) state items)
      (update-in state [:shapes id] geom/transform xfmt))))
