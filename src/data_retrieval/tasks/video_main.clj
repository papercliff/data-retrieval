(ns data-retrieval.tasks.video-main
  (:require [data-retrieval.tasks.video.meta-transform :as meta-transform]
            [data-retrieval.tasks.video.transform :as transform]))

(defn -main []
  (do
    (transform/save-important-nodes)
    (transform/save-important-edges)
    (transform/save-graph)
    (transform/save-clusters)
    (transform/save-clustered-graph)
    (transform/save-diffs)
    (transform/save-actions)
    (meta-transform/save-actions-with-days))
  (System/exit 0))
