(ns treehunter.parser)

(def line-regex (re-pattern #"^([0-9]{1,2}\s+[A-Za-z]+\s+[0-9]{4})\s+([0-9.:,]+)\s+\[([A-Z]+)\]\s+.*?((?:com|org|net)[a-zA-Z.0-9]+):\s*(.*)$"))

(defn read-file [filename] 
  (with-open [rdr (clojure.java.io/reader filename)]
   (let [lines (line-seq rdr)]
     (group-seq (map parse-line lines) #(not (:matched %))))))

(defn parse-line [line]
  (let [parsed (re-matches line-regex line)
        result {:matched (not (nil? parsed))}]
    (if (:matched result)
      (assoc result :parsed (rest parsed))
      (assoc result :body line))))

(defn merge-log-groups [group]
  (let [primary (:parsed (first group))]
    {:date (nth primary 0)
     :time (nth primary 1)
     :type (nth primary 2)
     :class (nth primary 3)
     :message (reduce #(str %1 "\n" (:body %2)) (nth primary 4) (rest group))}
    ))

(defn group-seq 
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

(let [log (clojure.string/split (slurp "resources/small-sample-log") #"[\n\r]+")]
  (map #(count %) (group-seq (map parse-line log) #(not (:matched %)))))

(println (let [log (clojure.string/split (slurp "resources/small-sample-log") #"[\n\r]+")]
  (map merge-log-groups (group-seq (map parse-line log) #(not (:matched %))))))