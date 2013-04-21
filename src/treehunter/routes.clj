(ns treehunter.routes
  (:use compojure.core
        ring.util.response)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.json :as json-middleware]
            [treehunter.db :as db]
            [treehunter.services :as services]
            [treehunter.config :as conf]
            [treehunter.parser :as parser]))

;;
;; Routing setup
;;

(defroutes api-routes 
  (GET "/" [] (response {:hello "Hello World"}))
  (GET "/stats/" [] (response (db/find-counts-by-source-type services/*dao*))))

(def ^:private root-dir {:root "public/app"})

(defroutes app-routes
  ;; REST API
  (context "/api" [] api-routes)
  ;; serve index.html at /
  (GET "/" [] (resource-response "index.html" root-dir))
  ;; static file serving
  (route/resources "/" root-dir)
  ;; 404 handler
  (route/not-found (resource-response "404.html" root-dir)))