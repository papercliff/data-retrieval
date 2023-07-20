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

(defn now []
  (time/now))
