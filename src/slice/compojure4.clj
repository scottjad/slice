(ns slice.compojure4
  (:use slice.core)
  (:require compojure.response))

(defmethod compojure.response/render slice.core.Slice [req m]
  (compojure.response/render req (render m)))
