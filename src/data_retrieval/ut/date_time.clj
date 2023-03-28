(ns data-retrieval.ut.date-time
  (:require [clj-time.core :as time]
            [clj-time.format :as time-f]
            [clojure.string :as s]))

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

(defn ->eee-mmm-dd-yyyy-str [dt]
  (-> "EEE MMM dd YYYY"
      time-f/formatter
      (time-f/unparse dt)
      s/upper-case))

(defn now []
  (time/now))

(defn prev-week-days [now]
  (let [prev-day (at-start-of-prev-day now)

        end-of-prev-week
        (if (= (time/day-of-week prev-day) 7)
          prev-day
          (->> prev-day
               (iterate at-start-of-prev-day)
               (take-while
                 #(not= (time/day-of-week %) 7))
               last
               at-start-of-prev-day))]
    (->> end-of-prev-week
         (iterate at-start-of-prev-day)
         (take 7)
         reverse)))
