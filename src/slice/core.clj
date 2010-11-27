(ns slice.core
  (:use [clojure.contrib.ns-utils :only (immigrate)]
        [clojure.contrib.def :only (defn-memo)])
  (:require [hiccup.core :as hiccup]
            [hiccup.page-helpers :as page-helpers]
            [com.reasonr.scriptjure :as scriptjure]
            [gaka.core :as gaka]))

(def *slice-memoize* false)

(defn slice-memoize! [b]
  (alter-var-root #'*slice-memoize* (constantly b)))

(defn- javascript-tag [s]
  [:script {:type "text/javascript"} (str "//<![CDATA[\n" s "\n//]]>")])

(defn- css-tag [s]
  [:style {:type "text/css"}
   (str "/*<![CDATA[*/\n" s "\n/*]]>*/")])

(defmacro just-html [& body] `(hiccup/html ~@body))

(defmacro just-js [& body] `(scriptjure/js ~@body))

(defmacro just-css [& body] `(gaka/css ~@body))

(defmacro js* [& body] `(scriptjure/js* ~@body))

(defrecord Slice [])

(defn slice? [x]
  (instance? slice.core.Slice x))

(defmacro js [& body] `(assoc (Slice.) :js (seq [(scriptjure/js ~@body)])))

(defmacro dom [& body] `(assoc (Slice.) :dom (seq [(scriptjure/js ~@body)])))

(defmacro html [& body] `(assoc (Slice.) :html (seq [(hiccup/html ~@body)])))

(defmacro css [& body] `(assoc (Slice.) :css (seq [(gaka/css ~@body)])))

(defmacro head [& body] `(assoc (Slice.) :head (seq [(hiccup/html ~@body)])))

(defn title [s] (assoc (Slice.) :title [s]))

(defn to-slice
  "If given a function, call it. Otherwise return what given. For using slices
  without enclosing ()"
  [sl]
  (if (fn? sl) (sl) sl))

(defn concat-or [a b]
  (if (or (coll? a) (coll? b))
    (concat a b)
    (or a b)))

(defn slices
  "Combine slice parts with concat keeping them as vectors"
  [& maps]
  (reduce #(update-in %1 %2 distinct)
          (apply merge-with concat-or (map to-slice maps))
          (map vector [:html :head :title :js :css :dom])))

(defmacro slice
  "Defines a slice. Slices are functions. If their arglist is empty it can be
  ommited. Their body is merged into a map, so top-level forms in a slice
  should return a map"
  [name args & body]
  (let [body (if (vector? args) body (cons args body))
        args (if (vector? args) args [])
        impure? (or (some :impure (map #(meta (if (slice? %)
                                                %
                                                (if (list? %)
                                                  (resolve (symbol (first %)))
                                                  (resolve (symbol %)))))
                                       body))
                    (:impure (meta name)))]
    (if *slice-memoize*
      (if impure?
        `(let [var# (defn ~name ~args (slices ~@body))]
           (alter-meta! var# assoc :impure true)
           var#)
        (if (= args [])
          `(let [val# (slices ~@body)]
             (defn ~name [] val#))
          `(defn-memo ~name ~args (slices ~@body))))
      `(defn ~name ~args (slices ~@body)))))

(defn-memo render-int
  ([sl]
     ;; TODO potential for optimizing by prerendering pure slices. either render could return a function 
     (let [{:keys [title html css js dom head]} sl]
       (hiccup/html
        [:html
         (when (or title head)
           [:head (when title [:title (apply str (interpose " - " title))])
            (when head (apply #(hiccup/html %&) head))])
         [:body
          (when html (apply #(hiccup/html %&) html))
          (when css (css-tag (apply str css)))
          ;; TODO fix ugly interposing ;
          (when js (javascript-tag (apply str (interpose ";" js))))
          (when dom (javascript-tag (scriptjure/js ($ (fn [] (quote (clj (apply str (interpose ";" dom)))))))))]]))))

(defn render [sl & sls]
  ;; separate from render-int so slices passed as functions always get invoked
  ;; looked up in memoized render
  (render-int (apply slices sl sls)))

(slice jquery [& [version]]
  (head (page-helpers/include-js
         (str "http://ajax.googleapis.com/ajax/libs/jquery/" (or version "1.4.2") "/jquery.min.js"))))

(defn wodot [s]
  (and s (str (.replace s "." ""))))

(defn wdot [s]
  (str "." (wodot s)))

(defn wo# [s]
  (and s (.replace s "#" "")))

(defn w# [s]
  (str "#" (wo# s)))

(defmacro dice
  "for advanced merging of slices"
  [[name sl key & more] & body]
  `(let [sl# (to-slice ~sl)
         ~name (~key sl#)]
     (slices (dissoc sl# ~key)
             ~(if more
                `(dice ~more ~@body)
                `(slices ~@body)))))

(defmacro let-html [[& bindings] & body]
  `(dice [~@(mapcat #(concat % [:html]) (partition 2 bindings))]
         ~@body))

(slice div [id sl]
  (dice [h sl :html] (html [:div {:id (wo# id)} h])))
