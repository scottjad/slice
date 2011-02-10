(ns slice.grid
  (:use slice.core
        slice.reset
        uteal.core
        uteal.test))

;;; grid
(defn dimensions
  "Returns column and gutter widths."
  [fluid? container-width number-columns gutter-percent]
  (let [container-width    (if fluid? 100 container-width)
        total-gutter-width (* container-width gutter-percent)
        number-gutters     number-columns
        gutter-width       (/ total-gutter-width number-gutters)
        total-column-width (- container-width total-gutter-width)
        column-width       (/ total-column-width number-columns)]
    (as-hash column-width gutter-width)))

(defn column-seq [{:keys [column-width gutter-width]} number-columns]
  (take number-columns
        (iterate (fn [so-far] (+ so-far column-width gutter-width))
                 column-width)))

(slice grid-i [i width gutter-width prefix]
  (let [total-width (+ width gutter-width)
        half-gutter (/ gutter-width 2)]
    (css [(str "." prefix "_" i)
          :margin-left half-gutter
          :margin-right half-gutter
          :width width
          :float :left
          :display :block
          :position :relative]
         [(str ".prefix_" i)
          :padding-left total-width]
         [(str ".suffix_" i)
          :padding-right total-width]
         [(str ".push_" i)
          :left total-width]
         [(str ".pull_" i)
          :left (- total-width)])))

(slice grid-helper [container-width max-width]
  (css [".alpha" :margin-left 0]
       [".omega" :margin-right 0]
       [".container"
        (if max-width
          (list :min-width container-width
                :max-width max-width)
          (list :width container-width))
        :margin :auto]))

(defn in
  "Returns a css class string. `opt` is :push, :pull, :prefix, or :suffix"
  [grid n & [opt]]
  (if (not (<= 0 n (:number-columns grid)))
    (wtf "grid doesn't have that many columns")
    (str (if opt
           (name opt)
           (:prefix grid)) "_" n)))

(slice new-grid [& {:keys [container-width max-width number-columns gutter-percent prefix]}]
  (merge (let [{:as widths :keys [gutter-width]}
               (dimensions false container-width number-columns gutter-percent)]
           (apply slices
                  (grid-helper container-width max-width)
                  (map grid-i
                       (iterate inc 1)
                       (column-seq widths number-columns)
                       (repeat gutter-width)
                       (repeat (or prefix "grid")))))
         {:number-columns number-columns
          :prefix prefix}))

(defmacro defgrid
  "Defines a fn `name` that returns a selector when passed args otherwise
  returns a grid slice."
  [name grid]
  `(let [s# ~grid]
     (defn ~name ([~'& access#]
       (if access#
         (apply in s# access#)
         s#))
       {:slice true})))

(defgrid agrid
  (new-grid :prefix "grid" :container-width 960 :number-columns 12 :gutter-percent 0.2))

(defn dot [& xs]
  (apply str (interpose \. xs)))

(slice grid-test
  agrid
  ;; global-reset
  (html 
   ["div.container"
        [(dot "div" (agrid 6) (agrid 3 :push))
         "logo"]
        ;; ["div.grid_3.pull_6" "text column"]
        [(dot "div" (agrid 3) (agrid 6 :pull))
         "text column"]
        ["div.grid_3" "text column"]
        ["div.grid_12" "big box"]]))
