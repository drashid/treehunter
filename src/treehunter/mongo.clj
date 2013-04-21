(ns treehunter.mongo
  (:use [clj-time.core :only [now]])
  (:require [treehunter.config :as conf]
            [monger.core :as mg]
            [monger.collection :as mc]
            [treehunter.db :as db]            
            [monger.joda-time]))

(def ^:private log-collection (conf/path conf/db :mongo :log-collection))
(def ^:private log-status-collection (conf/path conf/db :mongo :files-collection))

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
  
  (file-processing-started? [this filename] (not (nil? (mc/find-one log-status-collection {:filename filename}))))
  (set-file-status! [this filename status] (set-file-status filename status))
  (insert-logs! [this item-list] (mc/insert-batch log-collection item-list))
  
  )

