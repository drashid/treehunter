(ns treehunter.parser)

(def line-regex (re-pattern #"^([0-9]{1,2} [A-Za-z]+ [0-9]{4})(.*)$"))

;(defn read-file [filename] 
;  (with-open [rdr (clojure.java.io/reader filename)]
;   (let [lines (line-seq rdr)]
;     (map #(re-matches line-regex %) lines))))

(defn parse-line [line]
  (let [parsed (re-matches line-regex line)]
    {:matched (not (nil? parsed))
     :parsed (rest parsed)
     :text line}))

(defn group-seq 
  ([lst group-with-prev?]
    (group-seq lst group-with-prev? []))
  ([lst group-with-prev? agg]
     (if (empty? lst)
       agg
       ;; recursively continue grouping
       (group-seq 
          ;; rest we haven't taken
          (drop-while #(group-with-prev? %) (rest lst))
          ;; pass through function
          group-with-prev?
          ;; append to our collection the next group
          (conj agg
           (flatten  
            [(first lst)
             (take-while #(group-with-prev? %) (rest lst))])))
      )))


(let [log (clojure.string/split (slurp "resources/small-sample-log") #"[\n\r]+")]
  (map #(count %) (group-seq (map parse-line log) #(not (:matched %)))))