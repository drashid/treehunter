(ns treehunter.mongo
  (:use [clj-time.core :only [now]])
  (:require [treehunter.config :as conf]
            [monger.core :as mg]
            [monger.collection :as mc]
            [treehunter.db :as db]            
            [monger.joda-time]))

(def ^:private log-collection "rawLogs")
(def ^:private log-status-collection "logFiles")

(defn- init-mongo! []
  (do
    (mg/connect! (select-keys (:mongo conf/db) [:host :port]))
    (mg/set-db! (mg/get-db (conf/path conf/db :mongo :db)))
    (mc/ensure-index log-status-collection (array-map :filename 1) {:unique true})))

(defn- set-file-status [filename status]
  (mc/update 
    log-status-collection {:filename filename} 
                          {:filename filename 
                           :status status
                           :date (now)} :upsert true))

(deftype MongoDao []
  db/LogDao
  (init! [this] (init-mongo!))
  (set-file-status! [this filename status] (set-file-status filename status))
  (insert-log! [this log-item] (mc/insert log-collection log-item)))

