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

(defroutes app-routes
  (context "/api" [] api-routes)
  (route/resources "/")
  (route/not-found (resource-response "404.html" {:root "public/app"})))