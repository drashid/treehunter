(ns treehunter.mongo
  (:require [treehunter.config :as conf]
            [monger.core :as mg]
            [monger.collection :as mc]
            [treehunter.db :as db]))

(defn init-mongo! []
  (do
    (mg/connect! (select-keys (:mongo conf/db) [:host :port]))
    (mg/set-db! (mg/get-db (conf/path conf/db :mongo :db)))))

(deftype MongoDao []
  db/LogDao
  (init! [this] (init-mongo!))
  (insert-log! [this log-item] (mc/insert log-item)))

