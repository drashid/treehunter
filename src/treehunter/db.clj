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
  
  

