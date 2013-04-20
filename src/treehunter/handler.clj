(ns treehunter.handler
  (:use compojure.core
        ring.util.response)
  (:require [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.middleware.json :as json-middleware]))

(defroutes app-routes
  (GET "/" [] (response {:hello "Hello World"}))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site 
    (-> app-routes 
        json-middleware/wrap-json-response
        json-middleware/wrap-json-body)))
