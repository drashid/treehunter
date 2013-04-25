(ns treehunter.routes
  (:use compojure.core
        ring.util.response
        [clj-time.format :only [formatter parse]])
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

;; Example: "2013-04-16T07:00:00.000Z"
(def ^:private iso-formatter (formatter "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))

(defn- parse-date [date-string]
  (when date-string 
    (parse iso-formatter date-string)))

(defroutes api-routes 
  
  (GET "/stats/counts" [] 
       (response (db/find-counts-by-source-type)))
  
  (GET "/search" {params :params} []
       (let [source (:source params)
             start (parse-date (:startdate params))
             end (parse-date (:enddate params))
             limit (read-string (or (:limit params) "1"))
             type (:type params)]
         (response (db/find-items limit 
                                  {:source source
                                   :start start
                                   :end end
                                   :type type})))))

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