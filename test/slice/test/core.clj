(ns slice.test.core
  (:use slice.core
        uteal.test))

;;; ripped from scriptjure
(defn strip [str]
  (clojure.string/trim (clojure.string/replace (clojure.string/replace str #"\n" " ")
                                               #"[ ]+" " ")))

(describe html
  (it "returns a slice"
    (slice? (html)))
  (it "is a collection of html snippets"
    (coll? (:html (html)))))

(describe css
  (it "returns a slice"
    (slice? (css)))
  (it "is a collection of html snippets"
    (coll? (:css (css)))))

(describe js
  (it "returns a slice"
    (slice? (js)))
  (it "is a collection of html snippets"
    (coll? (:js (js)))))

(describe dom
  (it "returns a slice"
    (slice? (dom)))
  (it "is a collection of html snippets"
    (coll? (:dom (dom)))))

(describe title
  (it "returns a slice"
    (slice? (title "foo")))
  (it "is a collection of titles"
    (coll? (:title (title))))
  (it "takes a string"
    (= "hi" (first (:title (title "hi")))))
  (it "can take multiple titles"
    (= "bye" (second (:title (title "hi" "bye"))))))

(describe slices
  (it "can be empty"
    (= (slices) (slice.core.Slice.)))
  (it "can have html"
    (let [s1 (slices (html [:p 1]))]
      (= (:html s1) ["<p>1</p>"])))
  (it "can have css"
    (let [s2 (slices (css [:p :color :blue]))]
      (= (map strip (:css s2)) ["p { color: blue;}"])))
)
;;; test html/css/js/dom behavior on strings, ints, vectors

;;; test slices merging

;;; test memoizing
;;; test walking substitutes html and allows parens optional
;;; test slice
;;; test optional parens
;;; test rendering
;;; test speed
