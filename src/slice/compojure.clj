(ns slice.compojure
  (:use slice.core)
  (:require compojure.response))

(if (resolve 'compojure.response/Renderable)
  (extend-type slice.core.Slice
    compojure.response/Renderable
    (render [this _]
            (compojure.response/render (slice.core/render this) _)))
  
  (defmethod compojure.response/render slice.core.Slice [req m]
    (compojure.response/render req (render m))))
