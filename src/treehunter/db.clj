(ns treehunter.db)

;;
;; DAO interface
;;

(defprotocol LogDao
  (init! [this])
  (set-file-status! [this filename status])
  (file-processing-started? [this filename])
  (insert-log! [this log-item]))
;;  (find-log-by-id [this id])
  
  

