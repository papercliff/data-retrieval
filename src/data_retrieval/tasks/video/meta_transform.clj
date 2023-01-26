(ns data-retrieval.tasks.video.meta-transform
  (:require [data-retrieval.tasks.video.transform :as transform]
            [data-retrieval.ut.date-time :as dt]
            [data-retrieval.ut.fs :as fs]))

(def actions-with-days-path
  "resources/actions-with-days.json")

(defn save-actions-with-days []
  (->> transform/actions-path
       fs/load-content
       (mapcat
         (fn [time actions]
           (cons
             {:action "change-day"
              :new_day (dt/->eee-d-str time)}
             actions))
         (dt/prev-month-days (dt/now)))
       (fs/save-content actions-with-days-path)))
