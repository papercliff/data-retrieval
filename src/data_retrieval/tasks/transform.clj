(ns data-retrieval.tasks.transform
  (:require [clojure.string :as s]
            [data-retrieval.tasks.collect :as collect]
            [data-retrieval.ut.date-time :as dt]
            [data-retrieval.ut.fs :as fs]
            [data-retrieval.ut.re-cluster :as re-cluster]
            [loom.graph :as loom]
            [loom.alg :as loom-alg]))

(defn- important-nodes-path [now]
  (format
    "resources/%s-important-nodes.json"
    (dt/->start-of-prev-day-str now)))

(defn- important-edges-path [now]
  (format
    "resources/%s-important-edges.json"
    (dt/->start-of-prev-day-str now)))

(defn- graph-path [now]
  (format
    "resources/%s-graph.json"
    (dt/->start-of-prev-day-str now)))

(defn- clusters-path [now]
  (format
    "resources/%s-clusters.json"
    (dt/->start-of-prev-day-str now)))

(defn- clustered-graph-path [now]
  (format
    "resources/%s-clustered-graph.json"
    (dt/->start-of-prev-day-str now)))

(defn- diffs-path [now]
  (format
    "resources/%s-diffs.json"
    (dt/->start-of-prev-day-str now)))

(defn actions-path [now]
  (format
    "resources/%s-actions.json"
    (dt/->start-of-prev-day-str now)))

(defn save-important-nodes [time]
  (let [collected (-> time collect/keywords-path fs/load-content)
        infimum-agencies (->> collected
                              (map
                                #(->> %
                                      (map :agencies)
                                      (apply min)))
                              (apply max))]
    (->> collected
         (map
           (fn [coll]
             (->> coll
                  (filter
                    (fn [{:keys [agencies]}]
                      (> agencies infimum-agencies)))
                  (map #(vector (:keyword %) (:agencies %))))))
         (fs/save-content
           (important-nodes-path time)))))

(defn save-important-edges [time]
  (let [collected (-> time collect/combinations-path fs/load-content)
        infimum-agencies (->> collected
                              (map
                                #(->> %
                                      (map :agencies)
                                      (apply min)))
                              (apply max))]
    (->> collected
         (map
           (fn [coll]
             (->> coll
                  (filter
                    (fn [{:keys [agencies]}]
                      (> agencies infimum-agencies)))
                  (map :story)
                  (map #(s/split % #"-"))
                  (mapcat
                    (fn [[a b c]]
                      [[a b]
                       [a c]
                       [b c]]))
                  distinct)))
         (fs/save-content
           (important-edges-path time)))))

(defn save-graph [time]
  (let [nodes (-> time important-nodes-path fs/load-content)
        edges (-> time important-edges-path fs/load-content)
        final-edges (map
                      (fn [nds dgs]
                        (let [node-words (map first nds)]
                          (->> dgs
                               (map
                                 (fn [[a b]]
                                   (filter
                                     #(or (= % a)
                                          (= % b))
                                     node-words)))
                               (filter
                                 #(= (count %) 2)))))
                      nodes edges)
        final-nodes (map
                      (fn [nds dgs]
                        (let [edge-words (set (apply concat dgs))]
                          (filter
                            #(edge-words (first %))
                            nds)))
                      nodes final-edges)]
    (fs/save-content
      (graph-path time)
      (map
        (fn [nds dgs]
          {:nodes nds
           :edges dgs})
        final-nodes final-edges))))

(defn- load-cluster-sets [time]
  (->> time
       graph-path
       fs/load-content
       (mapcat :edges)
       (apply loom/graph)
       loom-alg/connected-components
       (map set)))

(defn save-clusters [time]
  (let [prev-str-sets (-> time dt/at-start-of-prev-day load-cluster-sets)
        curr-str-sets (load-cluster-sets time)
        new-str-sets (re-cluster/new-groups prev-str-sets curr-str-sets)]
    (doseq [cluster (sort-by count new-str-sets)]
      (->> cluster
           sort
           (s/join " · ")
           println))
    (fs/save-content
      (clusters-path time)
      (re-cluster/str-sets->key-dict new-str-sets))))

(defn save-clustered-graph [time]
  (let [clusters (-> time clusters-path fs/load-content)]
    (->> time
         graph-path
         fs/load-content
         (map
           (fn [{:keys [nodes edges]}]
             {:nodes (map
                       (fn [[id wgt]]
                         {:id      id
                          :weight  wgt
                          :cluster ((keyword id) clusters)})
                       nodes)
              :edges (map
                       (fn [[s t]]
                         {:source s
                          :target t})
                       edges)}))
         (fs/save-content
           (clustered-graph-path time)))))

(defn save-diffs [time]
  (let [clustered-graph (-> time clustered-graph-path fs/load-content)]
    (->> clustered-graph
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
                   curr-edge-set (->> curr
                                      :edges
                                      (map #(set (vals %)))
                                      set)
                   prev-edge-set (->> prev
                                      :edges
                                      (map #(set (vals %)))
                                      set)]
               {:added-nodes   (remove
                                 #(prev-node-set (:id %))
                                 (:nodes curr))
                :removed-nodes (remove
                                 #(curr-node-set (:id %))
                                 (:nodes prev))
                :updated-nodes (filter
                                 #(curr-node-set (:id %))
                                 (:nodes prev))
                :added-edges   (remove
                                 #(prev-edge-set (set (vals %)))
                                 (:edges curr))
                :removed-edges (remove
                                 #(curr-edge-set (set (vals %)))
                                 (:edges prev))}))
           clustered-graph)
         (fs/save-content
           (diffs-path time)))))

(defn save-actions [time]
  (let [diffs (-> time diffs-path fs/load-content)]
    (->> diffs
         (map
           (fn [{:keys [added-nodes
                        removed-nodes
                        updated-nodes
                        added-edges
                        removed-edges]}]
             (concat
               (->> removed-edges
                    reverse
                    (map #(assoc % :action "remove-edge")))
               (->> removed-nodes
                    reverse
                    (map #(assoc % :action "remove-node")))
               (map #(assoc % :action "update-node") updated-nodes)
               (map #(assoc % :action "add-node") added-nodes)
               (map #(assoc % :action "add-edge") added-edges))))
         (fs/save-content
           (actions-path time)))))
