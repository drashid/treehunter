(ns treehunter.handler
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [compojure.handler :as handler]
            [treehunter.services :as services]
            [treehunter.routes :as api]
            [ring.middleware.json :as json-middleware]
            [ring.middleware.keyword-params :as params-middleware]))

;;
;; Server initialization - wired up in project.clj
;;

(def app
  (handler/site 
    (-> api/app-routes
        params-middleware/wrap-keyword-params
        json-middleware/wrap-json-response
        json-middleware/wrap-json-body)))

(defn init! []
  (do 
    (services/db-service-init!)
    (services/job-service-init!)))

;; run via 'lein ring server' or via this main method
;; TODO pass in config, port
(defn -main [& args]
  (init!)
  (jetty/run-jetty app {:port 3000}))