(ns treehunter.mongo
  (:use [clj-time.core :only [now in-secs days minus interval]]
        [clj-time.coerce :only [from-long]]
        [monger.operators])
  (:require [treehunter.config :as conf]
            [monger.core :as mg]
            [monger.collection :as mc]
            [monger.query :as query]
            [treehunter.db :as db]            
            [monger.joda-time])
  (:import [org.bson.types ObjectId]))

(def ^:private log-collection (conf/db :mongo :log-collection))
(def ^:private log-status-collection (conf/db :mongo :files-collection))
(def ^:private expire-days (days (conf/db :mongo :expire-in-days)))

(defn- init-mongo! []
  (do
    (mg/connect! (select-keys (conf/db :mongo) [:host :port]))
    (mg/set-db! (mg/get-db (conf/db :mongo :db)))
    (mc/ensure-index log-status-collection (array-map :filename 1) {:unique true})
    ;; TTL expiration 
    (let [now (now)
          expire-seconds (in-secs (interval (minus now expire-days) now))]
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

(defn- id-to-string [item]
  (assoc item :_id (.toString (:_id item))))

(defn- assoc-if [mp k v]
  (if v 
    (assoc mp k v)
    mp))

(defn- build-query [{:keys [source start end type]
                     :or {source nil
                          type nil
                          start nil 
                          end nil}}]
  (let [date-q {:datetime {$gte (or start (from-long 0)) 
                           $lte (or end (now))}}]
        (assoc-if 
         (assoc-if date-q :source source) 
         :type type)))


(defn- find-items [limit constraints]
  (let [find-q (build-query constraints)
        _ (println "Find Query: " find-q)]
    (map id-to-string
     (query/with-collection log-collection
      (query/find find-q)
      (query/sort {:datetime -1})
      (query/limit limit)))))

;
; Query to get the first example for a given signature with the aggregation framework
; db.logs.aggregate( { $group: { _id: "$signature", realId: {$first: "$_id"}, source: {$first: "$source" }} })
;

(deftype MongoDao []
  db/LogDao
  (init! [this] (init-mongo!))
  
  ;; insertion 
  (file-processing-started? [this filename] (not (nil? (mc/find-one log-status-collection {:filename filename}))))
  (set-file-status! [this filename status] (set-file-status filename status))
  (insert-logs! [this item-list] (mc/insert-batch log-collection item-list))
  
  ;; lookup
  (find-counts-by-source-type [this] (find-grouped-counts))
  (find-items [this limit constraints] (find-items limit constraints))
)


