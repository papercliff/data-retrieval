(ns data-retrieval.tasks.collect
  (:require [data-retrieval.apis.papercliff :as papercliff-api]
            [data-retrieval.apis.github :as github-api]
            [data-retrieval.ut.date-time :as dt]))

(defn keywords-path [now]
  (format
    "collected/%s-single-day-keywords.json"
    (dt/->prev-day-str now)))

(defn combinations-path [now]
  (format
    "collected/%s-single-day-combinations.json"
    (dt/->prev-day-str now)))

(defn- query-params [now offset]
  (merge
    (if offset {:offset offset} {})
    {:from (-> now
               dt/at-start-of-prev-day
               dt/->date-hour-str)
     :to (-> now
             dt/at-start-of-curr-day
             dt/->date-hour-str)}))

(defn- papercliff-data [endpoint path-f now]
  (->> [nil 100 200]
       (map (partial query-params now))
       (mapcat (partial papercliff-api/call endpoint))
       (filter
         (fn [{:keys [agencies]}]
           (>= agencies 3)))
       (github-api/save-content
         (path-f now))))

(defn save-keywords [now]
  (papercliff-data
    "keywords"
    keywords-path
    now))

(defn save-combinations [now]
  (papercliff-data
    "combinations"
    combinations-path
    now))
