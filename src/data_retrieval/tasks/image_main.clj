(ns data-retrieval.tasks.image-main
  (:require [data-retrieval.apis.github :as github-api]
            [data-retrieval.tasks.image.collect :as collect]
            [data-retrieval.tasks.image.transform :as transform]
            [data-retrieval.ut.date-time :as dt]
            [taoensso.timbre :as timbre])
  (:gen-class))

(defn -main []
  (let [now (dt/now)]
    (if (-> now
            transform/actions-path
            github-api/file-exists?)
      (timbre/info "actions file already exists")
      (do (timbre/info "image task started")
          (collect/save-keywords now)
          (collect/save-combinations now)
          (transform/save-important-nodes now)
          (transform/save-important-edges now)
          (transform/save-graph now)
          (transform/save-clusters now)
          (transform/save-clustered-graph now)
          (transform/save-actions now)
          (timbre/info "image task completed")))))

(comment
  (do (require '[data-retrieval.ut.fs :as fs])
      (with-redefs [github-api/file-exists? (fn [path]
                                              (fs/file-exists?
                                                (str "../historical-data/" path)))
                    github-api/load-content (fn [path]
                                              (fs/load-content
                                                (str "../historical-data/" path)))
                    github-api/save-content (fn [path content]
                                              (fs/save-content
                                                (str "../historical-data/" path)
                                                content))]
        (-main)))
  (do (require '[clj-time.core :as tm])
      (let [time (tm/date-time 2022 11 6)]
        (-main time))))
