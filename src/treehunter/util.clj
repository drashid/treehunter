(ns treehunter.util
  (:import [org.bson.types ObjectId]))

(defn assoc-if
  "Given an input map and a set of key/values, associate each kv pair iff v is not nil.
   Example: (assoc-if {:a :a} :a nil :b :c) -> {:b :c, :a :a}"
  [mp & kv]
  (reduce 
   (fn [agg [k v]] (if v (assoc agg k v) agg)) 
   mp (apply hash-map kv)))

(defn objectid-to-string 
  "Given an input map, convert all top level ObjectId keys to Strings"
  [mp]
  (reduce
   (fn [agg [k v]] 
     (if (isa? (class v) ObjectId)
       (assoc agg k (.toString v))
       (assoc agg k v)))
   {} mp))

(defn to-word-freq 
  "String to word frequency map, split on any non-alphanumeric characters"
  ([string split-fn]
   (let [words (split-fn string)]
     (reduce #(assoc % %2 (inc (% %2 0))) {} words)))
  ([string]
   (to-word-freq string #(clojure.string/split (clojure.string/lower-case %) #"[^a-zA-Z0-9]+"))))

(defn group-seq
  "Group a sequence using the boolean operator provided
   Example: (group-seq [true false false true false] #(not (identity %)))
   Outputs: ((true false false) (true false))
  
   Evaluates semi-lazily (evaluates as far as it has to to hit the next non-grouped item)"
  [lst group-with-prev?]
    (lazy-seq 
      (let [next (first lst)
            grouped (or (take-while #(group-with-prev? %) (rest lst)) [])]
        (if (nil? next)
          lst
          (cons (cons next grouped) 
                (group-seq (drop (count grouped) (rest lst)) group-with-prev?))))))
