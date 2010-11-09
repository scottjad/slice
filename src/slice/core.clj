(ns slice.core
  (:use [clojure.contrib.ns-utils :only (immigrate)])
  (:require [hiccup.core :as hiccup]
            [hiccup.page-helpers :as page-helpers]
            [com.reasonr.scriptjure :as scriptjure]
            [cssgen :as cssgen]))

;; so cssgen's rule and mixin can be used by user w/o use
(immigrate 'cssgen)

;;; util
(defmacro by [x fname forms]
  `(do ~@(map (partial cons fname) (partition x forms))))

(defmacro defs [& args] `(by 2 def ~args))

(defn- cdata-tag
  "Wrap the supplied javascript or css up in script tags and a CDATA section."
  [type script]
  [:script {:type type}
    (str "//<![CDATA[\n" script "\n//]]>")])

(defn- javascript-tag [script]
  (cdata-tag "text/javascript" script))

(defn- css-tag [script]
  (cdata-tag "text/css" script))

(defmacro js
  "Translates code into js and returns {:js [\"translated-code\"]}. Use (clj foo)
  to include clj code foo in js."
  [& body]
  `{:js (seq [(scriptjure/js ~@body)])})

(defmacro dom
  "Translates code into js that runs after dom is loaded and returns {:dom
  [\"translated-code\"]}."
  [& body]
  `{:dom (seq [(scriptjure/js ~@body)])})

(defmacro html
  "Translates code into html and returns {:html [\"translated-code\"]}."
  [& body]
  `{:html (seq [(hiccup/html ~@body)])})

(defmacro css
  "Translates code into css and returns {:css [\"translated-code\"]}."
  [& body]
  `{:css (seq [(cssgen/css ~@body)])})

(defmacro head
  [& body]
  `{:head (seq [~@body])})

(defn title
  "Add a title to html page"
  [s]
  {:title [s]})

(defn merge-slices
  "Combine slice parts with concat keeping them as vectors"
  [& maps]
  (-> (apply merge-with concat
             (map #(dissoc % :slice)
                  (map #(if (fn? %) (%) %)
                       maps)))
      (assoc :slice true)))

;;; adopted from hiccup
(defn add-optional-map-arg
  "Add an optional map argument to a function that returns merges it with normal result"
  [func]
  (fn [& args]
    (if (and (map? (first args)) (:slice (first args)))
      (let [old (first args)
            new (apply func (rest args))]
        (assert (map? old))
        (assert (map? new))
        (merge-slices old new))
      (apply func args))))

(defmacro defelem
  "needs better name, comes hiccup, decorates a function"
  [name & fdecl]
  `(do (defn ~name ~@fdecl)
       (alter-var-root (var ~name) add-optional-map-arg)
       (var ~name)))

(defmacro slice
  "Defines a slice. Slices are functions that have been decorated to take an
  optional first arg that's a map, and if their arglist is empty it can be
  ommited. Their body is merged into a map, so top-level forms in a slice
  should return a map"
  [name args & body]
  (if (vector? args)
    `(defelem ~name ~args (merge-slices ~@body))
    `(defelem ~name [] (merge-slices ~args ~@body))))

(defn render [sl]
  ;; TODO make optional and fix so works > 31 elements where #{} ain't sorted
  (let [unique #(into #{} %)
        {:keys [title html css js dom head]} (if (fn? sl) (sl) sl)]
    (hiccup/html
     [:html
      (when (or title head)
        [:head (when title [:title (apply str (interpose " - " title))])
         (when head (apply #(hiccup/html %&) head))
         (when css (css-tag (apply str (unique css))))
         (when js (javascript-tag (apply str (unique js))))
         (when dom (javascript-tag (scriptjure/js ($ (fn [] (apply str (unique dom)))))))])
      (when html [:body (apply #(hiccup/html %&) (unique html))])])))

(slice jquery [& [version]]
  (head (page-helpers/include-js
         (str "http://ajax.googleapis.com/ajax/libs/jquery/" (or version "1.4.2") "/jquery.min.js"))))

(defn dot [s]
  (str "." s))

(defmacro update-html [[name sl] & body]
  `(update-in ~sl [:html] (fn [html#] (let [~name html#] (:html (html ~@body))))))
