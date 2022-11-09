(ns data-retrieval.ut.fs
  (:require [clojure.data.json :as json]))

(defn save-content
  [path contents]
  (spit
    path
    (json/write-str
      contents
      :indent true)))

(defn load-content [path]
  (json/read-str
    (slurp path)
    :key-fn keyword))
