(ns data-retrieval.ut.re-cluster
  (:require [clojure.set :as st]))

(defn- empty-vec-of-sets [len]
  (mapv
    (constantly #{})
    (range len)))

(defn str-sets->key-dict [sets]
  (->> sets
       (mapcat
         (fn [i nds]
           (map
             #(vector % i)
             (map keyword nds)))
         (range))
       (into {})))

(defn key-dict->str-sets [dict]
  (let [init-vec (->> dict
                      (map second)
                      (apply max)
                      inc
                      empty-vec-of-sets)]
    (reduce
      (fn [acc [x i]]
        (update acc i #(st/union % #{(name x)})))
      init-vec
      dict)))

(defn- similarity [xs ys]
  (/ (count (st/intersection xs ys))
     (count (st/union xs ys))))

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
                                   first))]
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
                      empty-vec-of-sets)]
    (reduce
      (fn [acc [i x]] (assoc acc i x))
      init-vec
      final-picked-pairs)))
