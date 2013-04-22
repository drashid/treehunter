(ns treehunter.mongo
  (:use [clj-time.core :only [now in-secs days minus interval]]
        [monger.operators])
  (:require [treehunter.config :as conf]
            [monger.core :as mg]
            [monger.collection :as mc]
            [treehunter.db :as db]            
            [monger.joda-time]))

(def ^:private log-collection (conf/path conf/db :mongo :log-collection))
(def ^:private log-status-collection (conf/path conf/db :mongo :files-collection))
(def ^:private expire-days (days (conf/path conf/db :mongo :expire-in-days)))

(defn- init-mongo! []
  (do
    (mg/connect! (select-keys (:mongo conf/db) [:host :port]))
    (mg/set-db! (mg/get-db (conf/path conf/db :mongo :db)))
    (mc/ensure-index log-status-collection (array-map :filename 1) {:unique true})
    ;; TTL expiration 
    (let [now (now)
          expire-seconds (in-secs 
                          (interval (minus now expire-days) now))]
      (mc/ensure-index log-collection (array-map :datetime 1)  
                                      {:expireAfterSeconds expire-seconds}))))

(defn- set-file-status [filename status]
  (mc/update 
    log-status-collection {:filename filename} 
                          {:filename filename 
                           :status status
                           :date (now)} :upsert true))

(defn- find-grouped-counts []
  (let [q-result (mc/aggregate log-collection 
                      [{$group {:_id {:source "$source"
                                      :type "$type"}
                                :count {$sum 1}}}])]
     (group-by #(:source %)
       (map #(assoc (:_id %) :count (:count %)) q-result))))

(deftype MongoDao []
  db/LogDao
  (init! [this] (init-mongo!))
  
  ;; insertion 
  (file-processing-started? [this filename] (not (nil? (mc/find-one log-status-collection {:filename filename}))))
  (set-file-status! [this filename status] (set-file-status filename status))
  (insert-logs! [this item-list] (mc/insert-batch log-collection item-list))
  
  ;; lookup
  (find-counts-by-source-type [this] (find-grouped-counts))
)


