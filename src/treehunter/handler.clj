(ns treehunter.handler
  (:use compojure.core
        ring.util.response
        [slingshot.slingshot :only [throw+]])
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [treehunter.routes :as api]
            [ring.middleware.json :as json-middleware]
            [treehunter.mongo :as mongo]
            [treehunter.config :as conf]
            [treehunter.parser :as parser])
  (:import [treehunter.mongo MongoDao]
           [treehunter.db LogDao]))

;;
;; DAO setup
;;

(def dao (ref {}))

(defn db-init! [] 
  (let [db (case (:type conf/db)
              "mongo" (MongoDao.)
              (throw+ "Only 'mongo' is a valid Database type!"))]
    (do
      (dosync (ref-set dao db))
      (.init! @dao))))

;;
;; Server initialization
;;

;; Wired up in :handler in project.clj
(def app
  (handler/site 
    (-> api/app-routes
        json-middleware/wrap-json-response
        json-middleware/wrap-json-body)))

;; Wired up in :init in project.clj
(defn init! []
  (do (db-init!)))

(defn -main [& args]
  (do
    (init!)
    (parser/process-file-to-db "resources/sample-log" @dao)))

(-main )