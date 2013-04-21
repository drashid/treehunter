(ns treehunter.routes
  (:use compojure.core
        ring.util.response)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.json :as json-middleware]
            [treehunter.mongo :as mongo]
            [treehunter.config :as conf]
            [treehunter.parser :as parser]))

;;
;; Routing setup
;;

(defroutes api-routes 
  (GET "/" [] (response {:hello "Hello World"})))

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