(ns data-retrieval.tasks.meta-transform
  (:require [data-retrieval.tasks.transform :as transform]
            [data-retrieval.ut.date-time :as dt]
            [data-retrieval.ut.fs :as fs]))

(defn- actions-with-day-path [now]
  (format
    "../historical-data/transformed/%s-actions-with-days.json"
    (dt/->start-of-week-str now)))

(defn save-actions-with-days [time]
  (->> time
       transform/actions-path
       fs/load-content
       (mapcat
         (fn [[_ to] actions]
           (cons
             {:action  "change-day"
              :new_day (-> to
                           dt/hour-str->
                           dt/prev-hour-pretty-str)}
             actions))
         (dt/running-week-pairs time))
       (fs/save-content
         (actions-with-day-path time))))
