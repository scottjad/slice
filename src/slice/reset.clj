(ns slice.reset
  (:use slice.core))

(def reset-box-model
  (list :margin :0
        :padding :0
        :border :0
        :outline :0))

(def reset-font
  (list :font-weight :inherit
        :font-style :inherit
        :font-size "100%"
        :font-family :inherit
        :vertical-align :baseline))

(def reset-body
  (list :line-height :1
        :color :black
        :background :white))

(def reset-list-style
  (list :list-style :none))

(def reset-table
  (list :border-collapse :separate
        :border-spacing :0
        :vertical-align :middle))

(def reset-table-cell
  (list  :text-align :left
         :font-weight :normal
         :vertical-align :middle))

(def reset-quotation
  (list :quotes ""
        (vector ":before, :after" :content "")))
       
(def reset-image-anchor-border
  (list :border :none))

(slice global-reset
  (css
   ["html, body, div, span, applet, object, iframe, h1, h2, h3, h4, h5, h6, p, blockquote, pre, a, abbr, acronym, address, big, cite, code, del, dfn, em, font, img, ins, kbd, q, s, samp, small, strike, strong, sub, sup, tt, var, dl, dt, dd, ol, ul, li, fieldset, form, label, legend, table, caption, tbody, tfoot, thead, tr, th, td"
    reset-box-model
    reset-font]
   ["body"
    reset-body]
   ["ol, ul"
    reset-list-style]
   ["table"
    reset-table]
   ["caption, th, td"
    reset-table-cell]
   ["q, blockquote"
    reset-quotation]
   ["a img"
    reset-image-anchor-border]))


