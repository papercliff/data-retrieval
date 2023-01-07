(ns data-retrieval.ut.fs
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [taoensso.timbre :as timbre]))

(defn file-exists? [path]
  (.exists (io/as-file path)))

(defn load-content [path]
  (timbre/info "loading contents from" path)
  (json/read-str
    (slurp path)
    :key-fn keyword))

(defn save-content
  [path content]
  (timbre/info "saving contents to" path)
  (spit
    path
    (json/write-str
      content
      :indent true)))
