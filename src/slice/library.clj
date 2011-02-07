(ns slice.library
  (:use slice.core
        [hiccup.page-helpers :as ph]))

(slice menu
  [links & {:as opts :keys [align id class]}]
  (let [class (or class "menu")]
    (slices (html (ph/unordered-list {:id id :class class} links))
            (css
             [(wdot class)
              :list-style :none
              :display :block
              (when-not (= :vertical align)
                ["li"
                 :float :left
                 :padding-right :10px])]))))

;;; problem:
;;; would like to create slices that require jquery but don't dictate where it
;;; comes from.

;;; how users choose alternatives:
;;; should be optional, and binding would require them to know which slices use jquery so they can wrap them. lame.

;;; concerns:
;;; will slices using jquery get compiled or cached to default before
;;; alternative is chosen?
(defonce jquery-source :remote)

(slice jquery [& [version]]
  (head (include-js
         (case jquery-source
           :remote (str "http://ajax.googleapis.com/ajax/libs/jquery/"
                        (or version "1.4.2")
                        "/jquery.min.js")
           :local   (str "/jquery/js/jquery-" (or version "1.4.2")
                         ".min.js")))))

(defonce jquery-mousewheel-source :remote)

(slice jquery-mousewheel
  jquery
  (head (include-js
         (case jquery-mousewheel-source
           :remote "http://jquery-ui.googlecode.com/svn-history/r2596/branches/dev/spinner/external/mousewheel/jquery.mousewheel.min.js"
           :local "/js/jquery.mousewheel.min.js"))))

(slice scrollable
  "a div that can be scrolled with the mousewheel and new content slides in"
  [content-divs & {:as opts :keys [scrollable-id width]}]
  jquery-mousewheel
  (let [n (count content-divs)
        width (if width width 500)
        widthpx (str width "px")
        scrollable-id (or scrollable-id (str "scrollable" (rand-int 100)))
        scrollable-fname (symbol scrollable-id)]
    (slices
     (js (fn between [min max x]
           (return (Math.min max (Math.max min x))))
         (fn ~scrollable-fname [shift]
           (.animate ($ (str ~(w# scrollable-id) " .items")) {:left shift} 400))
         (.mousewheel ($ ~(w# scrollable-id))
                      (fn [event delta]
                        (~scrollable-fname (between ~(* -1 (dec n) width)
                                                    0
                                                    (* ~width delta)))
                        (return false))))
     (html [:div {:id scrollable-id :class "scrollable"}
            [:div.items content-divs]])
     (css [(w# scrollable-id)
           :clear :both
           :position :relative
           :overflow :hidden
           :width widthpx
           [:div.items
            :position :relative
            :width :20000em
            :left 0
            [:div
             :display :block
             :width widthpx
             :float :left]]]))))

;; (scrollable (list [:div "foo"]
;;                   [:div "food"]))

(slice menu-scrollable
  "a menu attached to a scrollable div where the content changes either by
clicking the links in the menu or by using the mouse wheel"
  [content-titles content-divs scrollable-id & {:as opts :keys [width]}]
  (menu (map (fn [title item-num]
               (link-to-js ('~scrollable-id -fname ~(* width item-num)) title))
             content-titles
             (iterate dec 0)))
  (apply scrollable content-divs :scrollable-id scrollable-id
         (flatten (seq opts))))

;; (menu-scrollable ["say foo"
;;                   "say food"]
;;                  (list [:div "foo"]
;;                        [:div "food"])
;;                  (str "scrollable" (rand-int 100)))

