(ns treehunter.db)

;;
;; DAO interface
;;

(defprotocol LogDao
  ;; initialization
  (init! [this])
  
  ;; log parsing/insertion
  (file-processing-started? [this filename])
  (set-file-status! [this filename status])
  (insert-logs! [this item-list])
  
  ;; Returns something of the form:
  ;;  {'sourceA' [{'count' N 
  ;;               'type' 'INFO|ERROR|...' 
  ;;               'source' 'sourceA'}, ...] ...}
  (find-counts-by-source-type [this])
  
  (find-items-by-source [this source-class limit])
  
)

(def ^:dynamic ^LogDao *dao* nil)

(defn set-dao! [dao] 
  (alter-var-root (var *dao*) (fn [_] (identity dao))))

(defn init! [] (init! *dao*))

(defn file-processing-started? [filename] (file-processing-started? *dao* filename))

(defn set-file-status! [filename status] (set-file-status! *dao* filename status))

(defn insert-logs! [item-list] (insert-logs! *dao* item-list))

(defn find-counts-by-source-type [] (find-counts-by-source-type *dao*))

(defn find-items-by-source [source-class limit] (find-items-by-source *dao* source-class limit))
