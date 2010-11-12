(ns slice.compojure5
  (:use slice.core)
  (:require compojure.response))

(extend-type slice.core.Slice
  compojure.response/Renderable
  (render [this _]
          (compojure.response/render (slice.core/render this) _)))
  
