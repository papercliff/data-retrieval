(ns data-retrieval.ut.re-cluster
  (:require [clojure.set :as st]))

(defn- similarity [xs ys]
  (let [set-xs (set xs)
        set-ys (set ys)]
    (/ (count (st/intersection set-xs set-ys))
       (count (st/union set-xs set-ys)))))

(defn- go [prev-unpicked-pairs curr-unpicked curr-picked-pairs]
  (if (seq curr-unpicked)
    (let [just-picked-cluster (second (apply max-key (comp count second) curr-unpicked))
          just-picked-index (if (seq prev-unpicked-pairs)
                              (->> prev-unpicked-pairs
                                   (apply
                                     max-key
                                     #(similarity
                                        just-picked-cluster
                                        (second %)))
                                   first)
                              (->> (range)
                                   (remove
                                     (->> curr-picked-pairs (map first) set))
                                   second))]
      (go
        (remove
          (fn [[i _]]
            (= i just-picked-index))
          prev-unpicked-pairs)
        (remove
          (fn [c]
            (= (second c) just-picked-cluster))
          curr-unpicked)
        (cons
          [just-picked-index just-picked-cluster]
          curr-picked-pairs)))
    curr-picked-pairs))

(defn new-groups [prev-groups curr-groups]
  (let [final-picked-pairs (go
                             (map-indexed vector prev-groups)
                             (map-indexed vector curr-groups)
                             [])
        init-vec (->> [prev-groups curr-groups]
                      (map count)
                      (apply max)
                      range
                      (mapv (constantly [])))]
    (reduce
      (fn [acc [i x]] (assoc acc i x))
      init-vec
      final-picked-pairs)))
