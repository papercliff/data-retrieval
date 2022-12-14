(ns data-retrieval.tasks.video.meta-transform
  (:require [data-retrieval.tasks.video.transform :as transform]
            [data-retrieval.ut.date-time :as dt]
            [data-retrieval.ut.fs :as fs]))

(defn- actions-with-day-path [now]
  (format
    "../historical-data/transformed/%s-actions-with-hours.json"
    (dt/->start-of-prev-day-str now)))

(defn save-actions-with-days [time]
  (->> time
       transform/actions-path
       fs/load-content
       (mapcat
         (fn [[_ to] actions]
           (cons
             {:action   "change-hour"
              :new_hour (-> to
                            dt/date-hour-str->
                            dt/prev-hour
                            dt/->hour-minute-str
                            (str "\nUTC"))}
             actions))
         (dt/running-day-pairs time))
       reverse
       (cons
         {:action   "change-hour"
          :new_hour "00:00\nUTC"})
       reverse
       (fs/save-content
         (actions-with-day-path time))))
