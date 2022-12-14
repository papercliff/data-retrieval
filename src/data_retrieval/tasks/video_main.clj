(ns data-retrieval.tasks.video-main
  (:require [data-retrieval.tasks.video.collect :as collect]
            [data-retrieval.tasks.video.meta-transform :as meta-transform]
            [data-retrieval.tasks.video.transform :as transform]))

(defn -main [time]
  (do
    (collect/save-keywords time)
    (collect/save-combinations time)
    (transform/save-important-nodes time)
    (transform/save-important-edges time)
    (transform/save-graph time)
    (transform/save-clusters time)
    (transform/save-clustered-graph time)
    (transform/save-diffs time)
    (transform/save-actions time)
    (meta-transform/save-actions-with-days time)))

(comment
  (do (require '[data-retrieval.ut.date-time :as dt])
      (-main (dt/now)))
  (do (require '[data-retrieval.ut.date-time :as dt])
    (let [time (dt/now)]
      (transform/save-important-nodes time)
      (transform/save-important-edges time)
      (transform/save-graph time)
      (transform/save-clusters time)
      (transform/save-clustered-graph time)
      (transform/save-diffs time)
      (transform/save-actions time)
      (meta-transform/save-actions-with-days time)))
  (do (require '[clj-time.core :as tm])
      (let [time (tm/date-time 2022 11 6)]
        (-main time))))
