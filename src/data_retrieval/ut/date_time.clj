(ns data-retrieval.ut.date-time
  (:require [clj-time.core :as time]
            [clj-time.format :as time-f]))

(defn- at-start-of-prev-day [dt]
  (->> 1
       time/days
       (time/minus dt)
       time/with-time-at-start-of-day))

(defn ->hour-minute-str [dt]
  (time-f/unparse
    (time-f/formatter
      :hour-minute)
    dt))

(defn ->date-hour-str [dt]
  (time-f/unparse
    (time-f/formatter
      :date-hour-minute)
    dt))

(defn date-hour-str-> [v]
  (time-f/parse
    (time-f/formatter
      :date-hour-minute)
    v))

(defn ->start-of-prev-day-str [dt]
  (time-f/unparse
    (time-f/formatter :date)
    (at-start-of-prev-day dt)))

(defn now []
  (time/now))

(defn running-day-pairs [dt]
  (->> dt
       at-start-of-prev-day
       at-start-of-prev-day
       (iterate
         #(time/plus
            %
            (time/hours 1)))
       (take 25)
       (map
         #(vector
            (->date-hour-str %)
            (->date-hour-str
              (time/plus
                %
                (time/days 1)))))))
