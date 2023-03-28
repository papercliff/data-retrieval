(ns data-retrieval.tasks.video.transform
  (:require [data-retrieval.apis.github :as github-api]
            [data-retrieval.tasks.image.transform :as image-task]
            [data-retrieval.ut.date-time :as dt]
            [data-retrieval.ut.fs :as fs]))

(def diffs-path
  "resources/diffs.json")

(def actions-path
  "resources/actions.json")

(defn- daily-nodes-edges [day]
  (->> day
       dt/->day-str
       (format image-task/actions-path-fmt)
       github-api/load-content
       (map
         #(update % :action {"add-node" :nodes, "add-edge" :edges}))
       (group-by :action)))

(defn save-diffs []
  (let [week-nodes-edges (->> (dt/now)
                              dt/prev-week-days
                              (map daily-nodes-edges))]
    (->> week-nodes-edges
         (cons
           {:nodes []
            :edges []})
         (map
           (fn [curr prev]
             (let [curr-node-set (->> curr
                                      :nodes
                                      (map :id)
                                      set)
                   prev-node-set (->> prev
                                      :nodes
                                      (map :id)
                                      set)
                   added-nodes (remove
                                 #(prev-node-set (:id %))
                                 (:nodes curr))
                   added-node-set (set (map :id added-nodes))
                   curr-edge-set (->> curr
                                      :edges
                                      (map #(set (vals %)))
                                      set)
                   prev-edge-set (->> prev
                                      :edges
                                      (map #(set (vals %)))
                                      set)]
               {:added-nodes (remove
                               #(prev-node-set (:id %))
                               (:nodes curr))
                :removed-nodes (remove
                                 #(curr-node-set (:id %))
                                 (:nodes prev))
                :updated-nodes (remove
                                 #(added-node-set (:id %))
                                 (:nodes curr))
                :added-edges (remove
                               #(prev-edge-set (set (vals %)))
                               (:edges curr))
                :removed-edges (remove
                                 #(curr-edge-set (set (vals %)))
                                 (:edges prev))}))
           week-nodes-edges)
         (fs/save-content diffs-path))))

(defn save-actions []
  (->> diffs-path
       fs/load-content
       (map
         (fn [{:keys [added-nodes
                      removed-nodes
                      updated-nodes
                      added-edges
                      removed-edges]}]
           (concat
             (->> removed-nodes
                  reverse
                  (map #(assoc % :action "remove-node"))
                  shuffle)
             (map #(assoc % :action "remove-edge") removed-edges)
             (->> updated-nodes
                  (map #(assoc % :action "update-node"))
                  shuffle)
             (map #(assoc % :action "add-edge") added-edges)
             (->> added-nodes
                  (map #(assoc % :action "add-node"))
                  shuffle))))
       (fs/save-content actions-path)))
