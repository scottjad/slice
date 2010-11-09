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

(defn- javascript-tag [s]
  [:script {:type "text/javascript"} (str "//<![CDATA[\n" s "\n//]]>")])

(defn- css-tag [s]
  [:style {:type "text/css"}
   (str "/*<![CDATA[*/\n" s "\n/*]]>*/")])

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

(defn fslice [sl]
  (if (fn? sl) (sl) sl))

(defn slices
  "Combine slice parts with concat keeping them as vectors"
  [& maps]
  (-> (apply merge-with concat
             (map #(dissoc % :slice)
                  (map fslice maps)))
      (assoc :slice true)))

(defmacro slice
  "Defines a slice. Slices are functions that have been decorated to take an
  optional first arg that's a map, and if their arglist is empty it can be
  ommited. Their body is merged into a map, so top-level forms in a slice
  should return a map"
  [name args & body]
  (if (vector? args)
    `(defn ~name ~args (slices ~@body))
    `(defn ~name [] (slices ~args ~@body))))

(defn render
  ([sl]
     ;; TODO make optional and fix so works > 31 elements where #{} ain't sorted
     ;; TODO reverse in unique was added w/o thinking. perhaps better way
     (let [{:keys [title html css js dom head]} (fslice sl)]
       (hiccup/html
        [:html
         (when (or title head)
           [:head (when title [:title (apply str (interpose " - " title))])
            (when head (apply #(hiccup/html %&) (distinct head)))])
         [:body
          (when html (apply #(hiccup/html %&) (distinct html)))
          (when css (css-tag (apply str (distinct css))))
          ;; TODO fix ugly interposing ;
          (when js (javascript-tag (apply str (interpose ";" (distinct js)))))
          (when dom (javascript-tag (scriptjure/js ($ (fn [] (quote (clj (apply str (interpose ";" (distinct dom))))))))))]])))
  ([sl & sls]
     (render (apply slices sl sls))))

(slice jquery [& [version]]
  (head (page-helpers/include-js
         (str "http://ajax.googleapis.com/ajax/libs/jquery/" (or version "1.4.2") "/jquery.min.js"))))

(defn dot [s]
  (str "." s))

(defn id [s]
  (str "#" s))

(defn no# [s]
  (and s (.replace s "#" "")))

(defmacro update-html [[name sl] & body]
  `(update-in ~sl [:html] (fn [html#] (let [~name html#] (:html (html ~@body))))))

