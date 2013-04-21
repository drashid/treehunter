(ns treehunter.handler
  (:use compojure.core
        ring.util.response
        [slingshot.slingshot :only [throw+]])
  (:require [compojure.handler :as handler]
            [treehunter.services :as services]
            [treehunter.routes :as api]
            [ring.middleware.json :as json-middleware]
            [treehunter.mongo :as mongo]
            [treehunter.config :as conf]
            [treehunter.parser :as parser]))

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
    (services/db-init!)
    (services/job-init!)))

