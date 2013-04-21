(ns treehunter.services
  (:use [clojurewerkz.quartzite.jobs :only [defjob]]
        [clojurewerkz.quartzite.schedule.cron :only [schedule cron-schedule]]
        [slingshot.slingshot :only [try+ throw+]])
  (:require [treehunter.db :as db]
            [ring.middleware.json :as json-middleware]
            [treehunter.mongo :as mongo]
            [treehunter.config :as conf]
            [treehunter.parser :as parser]
            [clojurewerkz.quartzite.scheduler :as qs]
            [clojurewerkz.quartzite.jobs :as j]
            [clojurewerkz.quartzite.triggers :as t])
  (:import [treehunter.mongo MongoDao]
           [treehunter.db LogDao]
           [java.io File]))

;;
;; DAO setup
;;

(defn db-service-init! [] 
  (let [db (case (:type conf/db)
              "mongo" (MongoDao.)
              (throw+ "Only 'mongo' is a valid Database type!"))]
    (do
      (db/set-dao! db)
      (db/init!))))

;;
;; Recurring job to search for and parse new logs
;;

(def ^:private log-dir (:log-dir conf/parser))

(defn- files-under 
  "Return the list of files under the given directory path."
  [^String dir]
  (map #(.getAbsolutePath %) (filter #(.isFile %)(-> dir File. file-seq))))

(defjob ScanJob [ctx]
  (let [files (files-under log-dir)]
    (println "Scanning files under directory " log-dir)
    (try
     (dorun 
      (map #(if (db/file-processing-started? %)               
              (println (str "Skipping " %))
              (parser/process-file-to-db %))
           files))
     (catch Exception e
       (println "EXCEPTION PROCESSING LOGS: " (.getMessage e))
       (.printStackTrace e)))
    (println "Finished processing job.")))

(defn job-service-init! []
  (qs/initialize)
  (qs/start)
  (let [job (j/build
              (j/of-type ScanJob)
              (j/with-identity (j/key "jobs.filehunter")))
        trigger (t/build
                  (t/with-identity (t/key "triggers.filehunter"))
                  (t/start-now)
                  (t/with-schedule (schedule
                                     (cron-schedule (:cron-schedule conf/parser)))))]
    (qs/schedule job trigger)))