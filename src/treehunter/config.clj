(ns treehunter.config
  (:use [environ.core :only [env]])
  (:require [clj-yaml.core :as yaml]))

(def ^:private config 
  (let [config-file (or (env :config-file) "resources/config.yaml")
        _ (println "Loading config file " config-file)]
    (yaml/parse-string (slurp config-file))))

(defn path 
  "Given a nested map, traverse it with a set of keys and return the leaf"
  [root & path]
  (reduce #(%2 %1) root path))

;; probably a clean way to not have the code dupe below, maybe defmacro?

(defn parser 
  ([] (:parser config))
  ([& p] (apply path (cons (parser) p))))

(defn db 
  ([] (:db config))
  ([& p] (apply path (cons (db) p))))


