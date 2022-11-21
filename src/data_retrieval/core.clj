(ns data-retrieval.core
  (:require [data-retrieval.tasks.collect :as collect]
            [data-retrieval.tasks.meta-transform :as meta-transform]
            [data-retrieval.tasks.transform :as transform]))

(defn -main [time]
  (do
    (collect/save-keywords time)
    (collect/save-combinations time)
    (collect/save-daily-keywords time)
    (transform/save-important-nodes time)
    (transform/save-important-edges time)
    (transform/save-graph time)
    (transform/save-clusters time)
    (transform/save-clustered-graph time)
    (transform/save-diffs time)
    (transform/save-actions time)
    (meta-transform/save-actions-with-days time)
    (collect/print-daily-keywords time)))

(comment
  (do (require '[data-retrieval.ut.date-time :as dt])
      (-main (dt/now)))
  (do (require '[clj-time.core :as tm])
      (let [time (tm/date-time 2022 11 6)]
        (-main time))))
