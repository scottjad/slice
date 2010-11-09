(ns slice.example
  (:use slice.core
        hiccup.page-helpers
        hiccup.form-helpers))

(defs
  ;; strings
  company-name* "Cool Company"
  app-name*     "Awesome App"

  ;; colors
  site-color*     "blue"
  section-color*  "green"
  important-color* "red"

  ;; imgs
  logo-img*      "/img/logo.png"
  download-img*  "/img/download.png"
  subscribe-img* "/img/subscribe.png"

  ;; ids
  logo-id*     "#logo"
  download-id* "#download"
  subscribe-id* "#subscribe"

  ;; classes
  headers* "header"
  buttons* "button"

  ;; mixins
  rounded-corners* (mixin :-moz-border-radius :5px
                          :-webkit-border-radius :5px)
  big-text*        (mixin :font-size "200%")
  special-button*  (mixin rounded-corners* big-text*))

(slice awesome-effect
  jquery
  (js (fn awesomeEffect [div]
        (.effect ($ div) "highlight"))))

(slice mouse-effect [id]
  awesome-effect
  (dom (.mouseOver ($ (clj id)) (awesomeEffect (clj id)))))

(slice button [id text color]
  (html (submit-button {:id id :class buttons*} text))
  (css (rule (dot buttons*) rounded-corners*)))

(slice background-img [sel url]
  (css (rule sel :background-img url)))

(slice img-button [id text color url]
  (button id text color)
  (background-img id url))

(slice special-button [sel]
  (css (rule sel special-button*)))

(slice download-button
  (special-button download-id*)
  (img-button download-id* "Download!"  important-color* download-img*))

(slice subscribe-button
  jquery
  (dom (.click ($ (clj subscribe-id*))
               (fn [] (alert (clj (str "Subscribed to " company-name* " newsletter."))))))
  (img-button subscribe-id* "Subscribe!" section-color* subscribe-img*))

(slice header [text]
  (html [:h1 {:class headers*} text]))

(defn div [sl]
  (update-html [h (fslice sl)]
               [:div h]))

(slice site-header
  (mouse-effect logo-id*)
  (div (header company-name*))
  (css (rule logo-id* big-text*))) 

(slice app-section
  (div (header app-name*))
  download-button)

(slice main-page
  (title company-name*)
  site-header
  subscribe-button
  app-section)

(render main-page)

;; (defroutes foo
;;   (GET "/" _ (render main-page))
;;   (GET "/subscribe" _ (render (slices site-header subscribe-button)))
