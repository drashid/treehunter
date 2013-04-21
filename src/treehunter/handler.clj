(ns treehunter.handler
  (:require [compojure.handler :as handler]
            [treehunter.services :as services]
            [treehunter.routes :as api]
            [ring.middleware.json :as json-middleware]))

;;
;; Server initialization - wired up in project.clj
;;

(def app
  (handler/site 
    (-> api/app-routes
        json-middleware/wrap-json-response
        json-middleware/wrap-json-body)))

(defn init! []
  (do 
    (services/db-service-init!)
    (services/job-service-init!)))

