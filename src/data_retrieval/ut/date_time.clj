(ns data-retrieval.ut.date-time
  (:require [clj-time.core :as time]
            [clj-time.format :as time-f]))

(def at-start-of-curr-day
  time/with-time-at-start-of-day)

(defn at-start-of-prev-day [dt]
  (->> 1
       time/days
       (time/minus dt)
       time/with-time-at-start-of-day))

(defn ->date-hour-str [dt]
  (time-f/unparse
    (time-f/formatter
      :date-hour-minute)
    dt))

(defn ->day-str [dt]
  (time-f/unparse
    (time-f/formatter :date)
    dt))

(defn ->prev-day-str [dt]
  (-> dt
      at-start-of-prev-day
      ->day-str))

(defn ->eee-d-str [dt]
  (time-f/unparse
    (time-f/formatter
      "EEE\nd")
    dt))

(defn now []
  (time/now))

(defn prev-month-days [now]
  (let [one-month-ago (time/minus
                        now
                        (time/months 1))
        first-day (time/first-day-of-the-month- one-month-ago)
        last-day (time/last-day-of-the-month- one-month-ago)]
    (->> first-day
         (iterate
           #(time/plus
              %
              (time/days 1)))
         (map at-start-of-curr-day)
         (take-while
           #(time/before? % last-day)))))
