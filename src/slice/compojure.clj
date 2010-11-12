(ns slice.compojure
  (:use [compojure.core]
        slice.core))

(defmethod compojure.response/render java.util.Map [_ m]
  (let [m (if (:slice m) {:body (render m)} m)]
    (merge {:status 200, :headers {}, :body ""} m)))
