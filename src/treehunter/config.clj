(ns treehunter.config
  (:require [clj-yaml.core :as yaml]))

;; TODO generalize
(def ^:private config (yaml/parse-string (slurp "resources/config.yaml")))

(def parser (:parser config))

(defn path [root & path]
  (reduce #(%2 %1) root path))
