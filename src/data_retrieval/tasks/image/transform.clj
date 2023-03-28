(ns data-retrieval.tasks.image.transform
  (:require [clojure.string :as s]
            [data-retrieval.apis.github :as github-api]
            [data-retrieval.tasks.image.collect :as collect]
            [data-retrieval.ut.date-time :as dt]
            [data-retrieval.ut.fs :as fs]
            [data-retrieval.ut.re-cluster :as re-cluster]
            [loom.graph :as loom]
            [loom.alg :as loom-alg]
            [taoensso.timbre :as timbre]))

(defn- important-nodes-path [now]
  (format
    "resources/%s-single-day-important-nodes.json"
    (dt/->prev-day-str now)))

(defn- important-edges-path [now]
  (format
    "resources/%s-single-day-important-edges.json"
    (dt/->prev-day-str now)))

(defn- graph-path [now]
  (format
    "resources/%s-single-day-graph.json"
    (dt/->prev-day-str now)))

(defn- clusters-path [now]
  (format
    "transformed/%s-single-day-clusters.json"
    (dt/->prev-day-str now)))

(defn- clustered-graph-path [now]
  (format
    "resources/%s-single-day-clustered-graph.json"
    (dt/->prev-day-str now)))

(def actions-path-fmt
  "transformed/%s-single-day-actions.json")

(defn actions-path [now]
  (format
    actions-path-fmt
    (dt/->prev-day-str now)))

(defn save-important-nodes [now]
  (let [collected (-> now collect/keywords-path github-api/load-content)
        min-agencies (-> collected last :agencies inc)]
    (->> collected
         (filter
           (fn [{:keys [agencies]}]
             (>= agencies min-agencies)))
         (map #(vector (:keyword %) (:agencies %)))
         (fs/save-content
           (important-nodes-path now)))))

(defn save-important-edges [now]
  (let [collected (-> now collect/combinations-path github-api/load-content)
        min-agencies (-> collected last :agencies inc)]
    (->> collected
         (filter
           (fn [{:keys [agencies]}]
             (>= agencies min-agencies)))
         (map :story)
         (map #(s/split % #"-"))
         (mapcat
           (fn [[a b c]]
             [[a b]
              [a c]
              [b c]]))
         distinct
         (fs/save-content
           (important-edges-path now)))))

(defn- filtered-edges [nodes edges]
  (let [node-words (map first nodes)]
    (->> edges
         (map
           (fn [[a b]]
             (filter
               #(or (= % a)
                    (= % b))
               node-words)))
         (filter
           #(= (count %) 2)))))

(defn save-graph [now]
  (let [nodes (-> now important-nodes-path fs/load-content)
        edges (-> now important-edges-path fs/load-content)
        semi-final-edges (filtered-edges nodes edges)
        nodes-of-large-clusters (->> semi-final-edges
                                     (apply loom/graph)
                                     loom-alg/connected-components
                                     (filter #(> (count %) 3))
                                     (apply concat)
                                     set)
        final-nodes (let [edge-words (set (apply concat semi-final-edges))]
                      (filter
                        (fn [[nd _]]
                          (and
                            (edge-words nd)
                            (nodes-of-large-clusters nd)))
                        nodes))
        final-edges (filtered-edges final-nodes edges)]
    (fs/save-content
      (graph-path now)
      {:nodes final-nodes
       :edges final-edges})))

(defn save-clusters [now]
  (let [prev-clusters-path (->> now
                                (iterate dt/at-start-of-prev-day)
                                rest
                                (take 31)
                                (map clusters-path)
                                (filter github-api/file-exists?)
                                first)
        _ (timbre/infof
            "using %s as previous clusters path"
            prev-clusters-path)
        prev-str-sets (-> prev-clusters-path
                          github-api/load-content
                          re-cluster/key-dict->str-sets)
        curr-graph (->> now
                        graph-path
                        fs/load-content
                        :edges
                        (apply loom/graph))
        curr-str-sets (map
                        set
                        (loom-alg/connected-components curr-graph))
        new-str-sets (re-cluster/new-groups prev-str-sets curr-str-sets)]
    (github-api/save-content
      (clusters-path now)
      (re-cluster/str-sets->key-dict new-str-sets))))

(defn save-clustered-graph [now]
  (let [clusters (-> now clusters-path github-api/load-content)
        {:keys [nodes edges]} (-> now graph-path fs/load-content)]
    (fs/save-content
      (clustered-graph-path now)
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
                edges)})))

(defn save-actions [now]
  (let [{:keys [nodes edges]}
        (-> now
            clustered-graph-path
            fs/load-content)]
    (github-api/save-content
      (actions-path now)
      (concat
        (map #(assoc % :action "add-node") nodes)
        (map #(assoc % :action "add-edge") edges)))))
