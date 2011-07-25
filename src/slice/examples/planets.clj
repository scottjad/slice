(ns slice.examples.planets
  (:use compojure.core
        [slice core library compojure5]
        ring.util.serve))

(defonce planets [["Mercury" []]
                  ["Venus" []]
                  ["Earth" ["The Moon"]]
                  ["Mars" ["Phobos" "Deimos"]]
                  ["Jupiter" ["Io" "Europe" "Ganymedes" "Callisto"]]
                  ["Saturn" ["Titan" "Tethys" "Dione" "Rhea" "Iapetus"]]
                  ["Uranus" ["Miranda" "Ariel" "Umbriel" "Titania" "Oberon"]]
                  ["Neptune" ["Triton" "Proteus" "Nereid" "Larissa"]]])

(slice label [name]
  jquery
  (html (link-to-js (.html ($ "#details") ~name) name)))

(slice moon [name]
  (html [:li (label name)]))

(slice planet
  [[name moons]]
  (html (label name)
        [:ul (for [m moons]
               (moon m))]))

(slice tree
  (html [:div#tree (map planet planets)])
  (css ["#tree" {:float :left
                 :overflow :auto
                 :height "100%"
                 :width :200px}]))

(slice details
  (html [:div#details "select a planet/moon for details"]))

(slice page
  tree
  details)

(defroutes app
  (GET "/" _  (page)))

(serve #'app)
