(ns treehunter.parser
  (:use [clojurewerkz.quartzite.jobs :only [defjob]]
        [slingshot.slingshot :only [throw+ try+]])
  (:require [clojurewerkz.quartzite.scheduler :as qs]
            [treehunter.config :as conf]
            [clj-time.format :as time]
            [clojure.java.io :as io]
            [treehunter.db :as db])
  (:import [java.io File]))

;;
;; File parsing
;;

(def line-regex (re-pattern (conf/parser :line-regex)))
(def exception-regex (re-pattern (conf/parser :exception-regex)))

(def date-formatter (time/formatter (conf/path conf/parser :fields :datetime :format)))

(defn- group-seq
  [lst group-with-prev?]
    (lazy-seq 
      (let [next (first lst)
            grouped (or (take-while #(group-with-prev? %) (rest lst)) [])]
        (if (nil? next)
          lst
          (cons (cons next grouped) 
                (group-seq (drop (count grouped) (rest lst)) group-with-prev?))))))

(defn- parse-line 
  "Parse single line (potentially may be a non-primary line which will be grouped)"
  [line]
  (let [parsed (re-matches line-regex line)
        result {:matched (not (nil? parsed))}]
    (if (:matched result)
      (assoc result :parsed (rest parsed))
      (assoc result :body line))))

(defn- to-word-freq [string]
  (let [words (clojure.string/split (clojure.string/lower-case string) #"[^a-zA-Z0-9]+")]
   (reduce #(assoc % %2 (inc (% %2 0))) {} words)))

(defn- take-top-by-freq [string n]
  (map 
   first 
   (take n 
         ;; sort by count (higher first), and if equal counts by length (lower first)
         (sort-by identity #(let [k1 (first %1) v1 (second %1)
                                  k2 (first %2) v2 (second %2)]
                              (if (= v1 v2)
                                (< (count k1) (count k2))
                                (> v1 v2))) (to-word-freq string)))))

(defn- parse-log-groups 
  "Parse grouped logs"
  [group]
  (let [primary (:parsed (first group))
        fields (conf/parser :fields)
        ;; merge the rest of the bodys into the initial message
        message-body (reduce 
                        #(str %1 "\n" (:body %2)) 
                        (nth primary (conf/path fields :message :index)) 
                        (rest group))
        exception-matcher (re-matcher exception-regex message-body)
        entry {:datetime (time/parse date-formatter (nth primary (conf/path fields :datetime :index)))
               :type (nth primary (conf/path fields :type :index))
               :source (nth primary (conf/path fields :source :index))
               :message message-body
               :exceptions (filter #(identity %) 
                              (flatten 
                                (take-while 
                                  #(not (empty? %))
                                    (repeatedly #(rest (re-find exception-matcher))))))
               }]
    (assoc entry 
      :signature 
      (str (:type entry) "~"
           (:source entry) "~"
           (clojure.string/join "-" (take-top-by-freq (:message entry) 5))))))

(defn process-file-to-db [filename]
  (println "Parsing file and contents to DB: " filename)
  (let [is (if (.endsWith filename ".gz")
             (java.util.zip.GZIPInputStream. (io/input-stream filename))
             (io/input-stream filename))]
    (with-open [rdr (io/reader is)]
     (let [lines (line-seq rdr)
           log-entries (map parse-log-groups (group-seq (map parse-line lines) #(not (:matched %))))]
       (try+ 
         (db/set-file-status! filename :started)
         (db/insert-logs! log-entries)
         (db/set-file-status! filename :completed)
         (catch Object _
           (db/set-file-status! filename :failed)
           (throw+)))))))

