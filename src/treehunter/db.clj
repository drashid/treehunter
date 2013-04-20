(ns treehunter.db)

;;
;; DAO interface
;;

(defprotocol LogDao
  (init! [this])
  (insert-log! [this log-item]))
;;  (find-log-by-id [this id])
  
  

