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
  
  ;; log lookup
  
)

(defn file-processing-started? [dao filename] (.file-processing-started? dao filename))

(defn set-file-status! [dao filename status] (.set-file-status! dao filename status))

(defn insert-logs! [dao item-list] (.insert-logs! dao item-list))