(ns data-retrieval.core
  (:require [data-retrieval.ut.fs :as fs]
            [data-retrieval.tasks.collect :as collect]
            [data-retrieval.tasks.meta-transform :as meta-transform]
            [data-retrieval.tasks.transform :as transform]))

(def in-between-millis 450)
(def change-hour-millis 100)

(defn- go-transform [extra-threshold time]
  (println "extra-threshold" extra-threshold)
  (transform/save-important-edges extra-threshold time)
  (transform/save-graph time)
  (transform/save-clusters time)
  (transform/save-clustered-graph time)
  (transform/save-diffs time)
  (transform/save-actions time)
  (when (->> time
             transform/actions-path
             fs/load-content
             (apply concat)
             (filter
               (fn [{:keys [action]}]
                 (= action "add-edge")))
             count
             (* in-between-millis)
             (+ (* 25 change-hour-millis))
             (< 45000))
    (recur
      (inc extra-threshold)
      time)))

(defn -main [time]
  (do
    (collect/save-keywords time)
    (collect/save-combinations time)
    (transform/save-important-nodes time)
    (go-transform 0 time)
    (meta-transform/save-actions-with-days time)))

(comment
  (do (require '[data-retrieval.ut.date-time :as dt])
      (-main (dt/now))))
