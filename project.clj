(defproject treehunter "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [compojure "1.1.5"]
                 [ring/ring-json "0.2.0"]
                 [clojurewerkz/quartzite "1.0.1"]
                 [clj-yaml "0.4.0"]
                 [clj-time "0.5.0"]
                 [com.novemberain/monger "1.4.0"]
                 [slingshot "0.10.3"]]
  :plugins [[lein-ring "0.8.2"]]
  :ring {:handler treehunter.handler/app
         :init treehunter.handler/init! }
  :profiles
  {:dev {:dependencies [[ring-mock "0.1.3"]]}})
