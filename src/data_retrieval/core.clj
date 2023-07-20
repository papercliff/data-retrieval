(ns data-retrieval.core
  (:require [clojure.tools.logging :as log]
            [data-retrieval.apis.github :as github-api]
            [data-retrieval.tasks.collect :as collect]
            [data-retrieval.tasks.transform :as transform]
            [data-retrieval.ut.date-time :as dt])
  (:gen-class))

(defn -main []
  (let [now (dt/now)]
    (if (-> now
            transform/actions-path
            github-api/file-exists?)
      (log/info "actions file already exists")
      (do (log/info "task started")
          (collect/save-keywords now)
          (collect/save-combinations now)
          (transform/save-important-nodes now)
          (transform/save-important-edges now)
          (transform/save-graph now)
          (transform/save-clusters now)
          (transform/save-clustered-graph now)
          (transform/save-actions now)
          (log/info "task completed"))))
  (System/exit 0))
