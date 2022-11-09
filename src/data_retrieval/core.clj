(ns data-retrieval.core
  (:require [data-retrieval.ut.fs :as fs]
            [data-retrieval.tasks.collect :as collect]
            [data-retrieval.tasks.meta-transform :as meta-transform]
            [data-retrieval.tasks.transform :as transform]))

(def in-between-millis 450)

(defn- go-transform [extra-threshold time]
  (println "extra-threshold" extra-threshold)
  (transform/save-important-nodes extra-threshold time)
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
             (< 60000))
    (recur
      (inc extra-threshold)
      time)))

(defn- save-collections [time]
    (collect/save-keywords time)
    (collect/save-combinations time))

(defn -main [time]
  (do
    (save-collections time)
    (go-transform 0 time)
    (meta-transform/save-actions-with-days time)))
