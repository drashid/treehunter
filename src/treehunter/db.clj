(ns treehunter.db)

;;
;; DAO interface
;;

(defprotocol LogDao
  (init! [this])
  (set-file-status! [this filename status])
  (file-processing-started? [this filename])
  (insert-logs! [this item-list]))
  
  

