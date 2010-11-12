(ns slice.core
  (:use [clojure.contrib.ns-utils :only (immigrate)]
        [clojure.contrib.def :only (defn-memo)])
  (:require [hiccup.core :as hiccup]
            [hiccup.page-helpers :as page-helpers]
            [com.reasonr.scriptjure :as scriptjure]
            [cssgen :as cssgen]))

;; so cssgen's rule and mixin can be used by user w/o use
(immigrate 'cssgen)

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

(defmacro just-css [& body] `(cssgen/css ~@body))

(defmacro js* [& body] `(scriptjure/js* ~@body))

(defmacro js [& body] `{:slice true :js (seq [(scriptjure/js ~@body)])})

(defmacro dom [& body] `{:slice true :dom (seq [(scriptjure/js ~@body)])})

(defmacro html [& body] `{:slice true :html (seq [(hiccup/html ~@body)])})

(defmacro css [& body] `{:slice true :css (seq [(cssgen/css ~@body)])})

(defmacro head [& body] `{:slice true :head (seq ~@body)})

(defn title [s] {:slice true :title [s]})

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
  (apply merge-with concat-or (map to-slice maps)))

(defmacro slice
  "Defines a slice. Slices are functions. If their arglist is empty it can be
  ommited. Their body is merged into a map, so top-level forms in a slice
  should return a map"
  [name args & body]
  (let [body (if (vector? args) body (cons args body))
        args (if (vector? args) args [])
        impure? (or (some :impure (map #(meta (if (map? %)
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
           [:head (when title [:title (apply str (interpose " - " (distinct title)))])
            (when head (apply #(hiccup/html %&) (distinct head)))])
         [:body
          (when html (apply #(hiccup/html %&) (distinct html)))
          (when css (css-tag (apply str (distinct css))))
          ;; TODO fix ugly interposing ;
          (when js (javascript-tag (apply str (interpose ";" (distinct js)))))
          (when dom (javascript-tag (scriptjure/js ($ (fn [] (quote (clj (apply str (interpose ";" (distinct dom))))))))))]]))))

(defn render [sl & sls]
  ;; separate from render-int so slices passed as functions always get invoked
  ;; looked up in memoized render
  (render-int (apply slices sl sls)))

(slice jquery [& [version]]
  (head (page-helpers/include-js
         (str "http://ajax.googleapis.com/ajax/libs/jquery/" (or version "1.4.2") "/jquery.min.js"))))

(defn dot [s]
  (str "." s))

(defn id [s]
  (str "#" s))

(defn no# [s]
  (and s (.replace s "#" "")))

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
  (dice [h sl :html] (html [:div {:id (no# id)} h])))

;; Doesn't currently support handlers w/o (). Would need to add multimethod
;; render to compojure.response
(defn wrap-render-slice
  "Wrap an app such that slices are automatically rendered."
  [app]
  (fn [req]
    (let [resp (app req)]
      (if-not (and (map? resp) (:slice resp))
        resp
        (assoc resp :body (render resp))))))
