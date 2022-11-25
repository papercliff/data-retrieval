(ns data-retrieval.tasks.collect
  (:require [data-retrieval.ut.call :as call]
            [data-retrieval.ut.date-time :as dt]
            [data-retrieval.ut.fs :as fs]))

(defn keywords-path [now]
  (format
    "../historical-data/collected/%s-keywords.json"
    (dt/->start-of-prev-day-str now)))

(defn combinations-path [now]
  (format
    "../historical-data/collected/%s-combinations.json"
    (dt/->start-of-prev-day-str now)))

(defn- save-results [endpoint path-fun time]
  (->> time
       dt/running-day-pairs
       (map
         (fn [[from to]]
           (println to)
           (call/call
             endpoint
             {:from from
              :to   to})))
       (fs/save-content
         (path-fun time))))

(defn save-keywords [time]
  (save-results "keywords" keywords-path time))

(defn save-combinations [time]
  (save-results "combinations" combinations-path time))
