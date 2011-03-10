(ns slice.example
  (:use slice.core
        slice.compojure5                ; make slices render automatically
        slice.library                   ; prebuilt slices including jquery
        slice.grid                      ; a grid system (960 included)
        uteal.core                      ; defs FTW
        compojure.core                  ; slice isn't tied to a web "framework"
        ring.adapter.jetty
        [hiccup form-helpers page-helpers]))

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
  rounded-corners* (list :-moz-border-radius :5px
                         :-webkit-border-radius :5px)
  big-text*        (list :font-size "300%")
  special-button*  (list rounded-corners* big-text*))

(slice awesome-effect
  jquery
  (js (fn awesomeEffect [div]
        (.fadeOut ($ div))
        (.fadeIn ($ div)))))

(slice mouse-effect [id]
  awesome-effect
  (dom (.mouseover ($ ~id) (fn [] (awesomeEffect ~id)))))

(slice button [id text color]
  (html (submit-button {:id (wo# id) :class buttons*} text))
  (css [(wdot buttons*) rounded-corners* :color color]))

(slice on-click-alert [id msg]
  (dom (.click ($ ~id) (fn [] (alert ~msg)))))

(slice download-button
  (on-click-alert download-id* "Ain't slices cool?")
  (css [download-id* special-button*])
  (button download-id* "Download!" important-color*))

(slice subscribe-button
  jquery
  (on-click-alert subscribe-id* (str "Subscribed to " company-name* " newsletter."))
  (button subscribe-id* "Subscribe!" important-color*))

(slice header [text & [id]]
  (html [:h1 {:id (wo# id)} text]))

(slice site-header
  (mouse-effect logo-id*)
  (header company-name* logo-id*)
  (css [logo-id*
        big-text*
        :color site-color*]))

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
  (GET "/"          _       (main-page))
  (GET "/subscribe" _       (slices site-header subscribe-button))
  (GET "/test"      request (slices jquery
                                    (dom (alert ~(:remote-addr request)))
                                    (html [:h1 "Hi"])
                                    (css [:h1 :color "blue"])))
  (GET "/grid"      _       (slices agrid
				     (html 
				      ["div.container"
				       [(dot "div" (agrid 6))
					"1st column of 6"]
				       [(dot "div" (agrid 3))
					"1st column of next 3"]
				       [(dot "div" (agrid 3) (agrid 1 :pull))
					"final 3 but back 1"]
				       ;; ["div.grid_3.pull_6" "text column"]
				       ["div.grid_6" "1st column of 6"]
				       ["div.grid_3" "1st column of next 3"]
				       ["div.grid_3.pull_1" "final 3 but back 1"]
				       [(dot "div" (agrid 12)) "big box"]]))))

(defonce server (run-jetty #'app {:port 8888 :join? false}))
