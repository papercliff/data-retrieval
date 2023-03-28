(ns data-retrieval.tasks.video-main
  (:require [data-retrieval.tasks.video.meta-transform :as meta-transform]
            [data-retrieval.tasks.video.transform :as transform]))

(defn -main []
  (transform/save-diffs)
  (transform/save-actions)
  (meta-transform/save-actions-with-days)
  (System/exit 0))
