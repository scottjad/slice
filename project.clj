(defproject slice "0.6.0-SNAPSHOT"
  :description "Write composable slices of html, css, and js in Clojure"
  :url "http://github.com/scottjad/slice"
  :dependencies [[org.clojure/clojure "1.2.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [hiccup "0.3.1"]
                 [org.clojars.scottjad/gaka "0.2.1"]
                 [org.clojars.scottjad/scriptjure "0.1.20"]
                 [uteal "0.1.0-SNAPSHOT"]]
  :dev-dependencies [[ring/ring-devel "0.3.3"]
                     [ring/ring-jetty-adapter "0.3.3"]
                     [swank-clojure "1.3.0-SNAPSHOT"]
                     [compojure "0.5.2"]])
