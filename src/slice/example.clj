(ns slice.example
  (:use slice.core
        slice.compojure
        uteal.core
        compojure.core
        ring.adapter.jetty
        hiccup.form-helpers))

;; when deploying use
;; (slice-memoize! true)

(defs
  ;; strings
  company-name*    "Cool Company"
  app-name*        "Awesome App"

  ;; colors
  site-color*      "blue"
  important-color* "red"

  ;; ids
  logo-id*         "#logo"
  download-id*     "#download"
  subscribe-id*    "#subscribe"

  ;; classes
  buttons*         "button"

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
  (css (rule (dot buttons*) rounded-corners* :color color)))

(slice on-click-alert [id msg]
  (dom (.click ($ ~id) (fn [] (alert ~msg)))))

(slice download-button
  (on-click-alert download-id* "Ain't slices cool?")
  (css (rule download-id* special-button*))
  (button download-id* "Download!" important-color*))

(slice subscribe-button
  jquery
  (on-click-alert subscribe-id* (str "Subscribed to " company-name* " newsletter."))
  (button subscribe-id* "Subscribe!" important-color*))

(slice header [text & [id]]
  (html [:h1 {:id (no# id)} text]))

(slice site-header
  (mouse-effect logo-id*)
  (header company-name* logo-id*)
  (css (rule logo-id*
         big-text*
         :color site-color*)))

;;; impure slices and slices that use impure slices aren't memoized
(slice ^{:impure true} random-number
  (html [:p (rand-int 100)]))

(slice app-section
  (header app-name*)
  download-button)

(slice main-page
  (title company-name*)
  site-header
  subscribe-button
  app-section
  random-number)

(defroutes app
      (GET "/"          _ (main-page))
      (GET "/subscribe" _ (slices site-header subscribe-button))
      (GET "/test"      r (slices jquery
                                  (dom (alert ~(:remote-addr r)))
                                  (html [:h1 "Hi"])
                                  (css (rule "h1" :color "blue")))))

(defonce server (run-jetty #'app {:port 8888 :join? false}))
