(ns treehunter.config
  (:require [clj-yaml.core :as yaml]))

;; TODO generalize
(def ^:private config (yaml/parse-string (slurp "resources/config.yaml")))

(def parser (:parser config))

(def db (:db config))

(defn path 
  "Given a nested map, traverse it with a set of keys and return the leaf"
  [root & path]
  (reduce #(%2 %1) root path))
