(ns data-retrieval.ut.fs
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]))

(defn load-content [path]
  (log/info "loading contents from" path)
  (json/read-str
    (slurp path)
    :key-fn keyword))

(defn save-content
  [path content]
  (log/info "saving contents to" path)
  (spit
    path
    (json/write-str
      content
      :indent true)))
