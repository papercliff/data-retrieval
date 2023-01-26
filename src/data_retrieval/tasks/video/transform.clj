(ns data-retrieval.tasks.video.transform
  (:require [clojure.string :as s]
            [data-retrieval.ut.date-time :as dt]
            [data-retrieval.ut.fs :as fs]
            [data-retrieval.ut.re-cluster :as re-cluster]
            [loom.graph :as loom]
            [loom.alg :as loom-alg]))

(def important-nodes-path
  "resources/important-nodes.json")

(def important-edges-path
  "resources/important-edges.json")

(def graph-path
  "resources/graph.json")

(def clusters-path
  "resources/clusters.json")

(def clustered-graph-path
  "resources/clustered-graph.json")

(def diffs-path
  "resources/diffs.json")

(def actions-path
  "resources/actions.json")

(defn- prev-month-contents [fmt]
  (->> (dt/now)
       dt/prev-month-days
       (map dt/->day-str)
       (map (partial format fmt))
       (map fs/load-content)))

(defn save-important-nodes []
  (let [collected (prev-month-contents
                    "../historical-data/collected/%s-single-day-keywords.json")
        infimum-agencies (->> collected
                              (map
                                #(->> %
                                      (map :agencies)
                                      (take 100)
                                      last))
                              (apply max))]
    (->> collected
         (map
           (fn [coll]
             (->> coll
                  (filter
                    (fn [{:keys [agencies]}]
                      (> agencies infimum-agencies)))
                  (map #(vector (:keyword %) (:agencies %))))))
         (fs/save-content important-nodes-path))))

(defn save-important-edges []
  (let [collected (prev-month-contents
                    "../historical-data/collected/%s-single-day-combinations.json")
        infimum-agencies (->> collected
                              (map
                                #(->> %
                                      (map :agencies)
                                      (take 33)
                                      last))
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
         (fs/save-content important-edges-path))))

(defn- filtered-edges [nodes edges]
  (map
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
    nodes edges))

(defn save-graph []
  (let [nodes (fs/load-content important-nodes-path)
        edges (fs/load-content important-edges-path)
        semi-final-edges (filtered-edges nodes edges)
        nodes-of-large-clusters (->> semi-final-edges
                                     (apply concat)
                                     distinct
                                     (apply loom/graph)
                                     loom-alg/connected-components
                                     (filter #(> (count %) 3))
                                     (apply concat)
                                     set)
        final-nodes (map
                      (fn [nds dgs]
                        (let [edge-words (set (apply concat dgs))]
                          (filter
                            (fn [[nd _]]
                              (and
                                (edge-words nd)
                                (nodes-of-large-clusters nd)))
                            nds)))
                      nodes semi-final-edges)
        final-edges (filtered-edges final-nodes edges)]
    (fs/save-content
      graph-path
      (map
        (fn [nds dgs]
          {:nodes nds
           :edges dgs})
        final-nodes final-edges))))

(defn save-clusters []
  (->> graph-path
       fs/load-content
       (mapcat :edges)
       (apply loom/graph)
       loom-alg/connected-components
       (map set)
       re-cluster/str-sets->key-dict
       (fs/save-content clusters-path)))

(defn save-clustered-graph []
  (let [clusters (fs/load-content clusters-path)]
    (->> graph-path
         fs/load-content
         (map
           (fn [{:keys [nodes edges]}]
             {:nodes (map
                       (fn [[id wgt]]
                         {:id id
                          :weight wgt
                          :cluster ((keyword id) clusters)})
                       nodes)
              :edges (map
                       (fn [[s t]]
                         {:source s
                          :target t})
                       edges)}))
         (fs/save-content clustered-graph-path))))

(defn save-diffs []
  (let [clustered-graph (fs/load-content clustered-graph-path)]
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
               {:added-nodes (remove
                               #(prev-node-set (:id %))
                               (:nodes curr))
                :removed-nodes (remove
                                 #(curr-node-set (:id %))
                                 (:nodes prev))
                :updated-nodes (filter
                                 #(curr-node-set (:id %))
                                 (:nodes prev))
                :added-edges (remove
                               #(prev-edge-set (set (vals %)))
                               (:edges curr))
                :removed-edges (remove
                                 #(curr-edge-set (set (vals %)))
                                 (:edges prev))}))
           clustered-graph)
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
             (->> removed-edges
                  reverse
                  (map #(assoc % :action "remove-edge")))
             (->> removed-nodes
                  reverse
                  (map #(assoc % :action "remove-node")))
             (map #(assoc % :action "update-node") updated-nodes)
             (map #(assoc % :action "add-node") added-nodes)
             (map #(assoc % :action "add-edge") added-edges))))
       (fs/save-content actions-path)))
