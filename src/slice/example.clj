(ns slice.example
  (:use slice.core
        [compojure core route]
        ring.adapter.jetty
        hiccup.page-helpers
        hiccup.form-helpers))

(defs
  ;; strings
  company-name* "Cool Company"
  app-name*     "Awesome App"

  ;; colors
  site-color*      "blue"
  section-color*   "green"
  important-color* "red"

  ;; imgs
  logo-img*      "/img/logo.png"
  download-img*  "/img/download.png"
  subscribe-img* "/img/subscribe.png"

  ;; ids
  logo-id*      "#logo"
  download-id*  "#download"
  subscribe-id* "#subscribe"

  ;; classes
  headers* "header"
  buttons* "button"

  ;; mixins
  rounded-corners* (mixin :-moz-border-radius :5px
                          :-webkit-border-radius :5px)
  big-text*        (mixin :font-size "300%")
  special-button*  (mixin rounded-corners* big-text*))

(slice awesome-effect
  jquery
  (js (fn awesomeEffect [div]
        (.fadeOut ($ div))
        (.fadeIn ($ div)))))

(slice mouse-effect [id]
  awesome-effect
  (dom (.mouseover ($ ~id) (fn [] (awesomeEffect ~id)))))

(slice button [id text color]
  (html (submit-button {:id (no# id) :class buttons*} text))
  (css (rule (dot buttons*) rounded-corners*)))

(slice background-img [sel url]
  (css (rule sel :background-img url)))

(slice img-button [id text color url]
  (button id text color)
  (background-img id url))

(slice special-button [sel]
  (css (rule sel special-button*)))

(slice on-click-alert [id msg]
  (dom (.click ($ ~id) (fn [] (alert ~msg)))))

(slice download-button
  (special-button download-id*)
  (img-button download-id* "Download!" important-color* download-img*)
  (on-click-alert download-id* "Ain't slices cool?"))

(slice subscribe-button
  jquery
  (on-click-alert subscribe-id* (str "Subscribed to " company-name* " newsletter."))
  (img-button subscribe-id* "Subscribe!" section-color* subscribe-img*))

(slice header [text & [id]]
  (html [:h1 {:class headers* :id (no# id)} text]))

(defn div [sl & [id]]
  (update-html [h (fslice sl)] [:div {:id (no# id)} h]))

(slice site-header
  (mouse-effect logo-id*)
  (header company-name* logo-id*)
  (css (rule logo-id*
         big-text*
         :color :blue)))

;;; impure slices and slices that use impure slices aren't cached
(slice ^{:impure true} random-number
  (html [:p (rand-int 100)]))

(slice app-section
  (div (header app-name*))
  download-button)

(slice main-page
  (title company-name*)
  site-header
  subscribe-button
  app-section
  random-number)

(defroutes app
  (GET "/"          _ (render main-page))
  (GET "/subscribe" _ (render site-header subscribe-button))
  (GET "/test"      r (render jquery
                              (dom (alert ~(:remote-addr r)))
                              (html [:h1 "Hi"])
                              (css (rule "h1" :color "blue")))))

(defonce server (run-jetty #'app {:port 8888 :join? false}))