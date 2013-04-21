(ns treehunter.parser
  (:use [clojurewerkz.quartzite.jobs :only [defjob]]
        [slingshot.slingshot :only [throw+ try+]])
  (:require [clojurewerkz.quartzite.scheduler :as qs]
            [treehunter.config :as conf]
            [clj-time.format :as time])
  (:import [java.io File]
           [treehunter.db LogDao]))

;;
;; Recurring job to search for and parse new logs
;;

(def ^:private log-dir (:log-dir conf/parser))

(defn- files-under 
  "Return the list of files under the given directory path."
  [^String dir]
  (filter #(.isFile %)(-> dir File. file-seq)))

(defjob scan-job [ctx]
  (println (files-under log-dir)))

;;
;; File parsing
;;

(def line-regex (re-pattern (conf/parser :regex)))

(def date-formatter (time/formatter (conf/path conf/parser :fields :datetime :format)))

(defn- group-seq 
  "Group stream sequence using the boolean grouping function."
  ([lst group-with-prev?]
    (group-seq lst group-with-prev? []))
  ([lst group-with-prev? agg]
     (if (empty? lst)
       ;; base case, no more elements to consume
       agg
       ;; recursively continue grouping
       (let [next (first lst)
             grouped (take-while #(group-with-prev? %) (rest lst))]
         (recur
           (drop (count grouped) (rest lst))
           group-with-prev?
           (conj agg (cons next grouped)))))))

(defn- parse-line 
  "Parse single line (potentially may be a non-primary line which will be grouped)"
  [line]
  (let [parsed (re-matches line-regex line)
        result {:matched (not (nil? parsed))}]
    (if (:matched result)
      (assoc result :parsed (rest parsed))
      (assoc result :body line))))

(defn- parse-log-groups 
  "Parse grouped logs"
  [group]
  (let [primary (:parsed (first group))
        fields (conf/parser :fields)]
    {:datetime (time/parse date-formatter (nth primary (conf/path fields :datetime :index)))
     :type (nth primary (conf/path fields :type :index))
     :source (nth primary (conf/path fields :source :index))
     :message (reduce 
               #(str %1 "\n" (:body %2)) 
               (nth primary (conf/path fields :message :index)) 
               (rest group))
     }))

(defn read-log-file 
  "Parse log file into stream of maps for each entry"
  [filename] 
  (with-open [rdr (clojure.java.io/reader filename)]
   (let [lines (line-seq rdr)]
     (map parse-log-groups (group-seq (map parse-line lines) #(not (:matched %)))))))

(defn process-file-to-db [filename ^LogDao dao]
  (try+ 
   (.set-file-status! dao filename :started)
   (dorun 
     (map #(.insert-log! dao %) (read-log-file filename)))
   (.set-file-status! dao filename :completed)
   (catch Object _
     (.set-file-status! dao filename :failed)
     (throw+))))

;; TESTING CODE

(defn -main [& args]
  (println (read-log-file "resources/sample-log")))

(-main)
