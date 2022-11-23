(ns data-retrieval.tasks.collect
  (:require [clojure.string :as s]
            [data-retrieval.ut.call :as call]
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

(defn- daily-keywords-path [now]
  (format
    "../historical-data/collected/%s-daily-keywords.json"
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

(defn save-daily-keywords [time]
  (fs/save-content
    (daily-keywords-path time)
    (call/call
      "keywords"
      {:from (-> time
                 dt/at-start-of-prev-day
                 dt/at-start-of-prev-day
                 dt/->date-hour-str)
       :to   (-> time
                 dt/at-start-of-prev-day
                 dt/->date-hour-str)})))

(defn print-daily-keywords [time]
  (->> time
       daily-keywords-path
       fs/load-content
       (map :keyword)
       (map #(str "#" %))
       (s/join " ")
       println))
