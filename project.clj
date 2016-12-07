(defproject splts "1.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.apache.httpcomponents/httpclient "4.5.2"]
                 [com.google.guava/guava "20.0"]
                 [org.clojure/data.json "0.2.6"]
                 ]
  :min-lein-version "2.0.0"  
  :uberjar-name "splts-standalone.jar"
  :profiles {:production {:env {:production true}}})
