(ns data-retrieval.ut.date-time
  (:require [clj-time.core :as time]
            [clj-time.format :as time-f]))

(defn at-start-of-week [dt]
  (->> 1
       time/weeks
       (time/minus dt)
       time/with-time-at-start-of-day))

(defn ->hour-str [dt]
  (time-f/unparse
    (time-f/formatter
      :date-hour-minute)
    dt))

(defn hour-str-> [v]
  (time-f/parse
    (time-f/formatter
      :date-hour-minute)
    v))

(defn ->start-of-week-str [dt]
  (time-f/unparse
    (time-f/formatter :date)
    (at-start-of-week dt)))

(defn prev-hour-pretty-str [dt]
  (let [rfc822 (time-f/unparse
                 (time-f/formatter :rfc822)
                 (time/minus dt (time/hours 1)))]
    (str (subs rfc822 0 3)
         (subs rfc822 4 7)
         "\n"
         (subs rfc822 8 16))))

(defn now []
  (time/now))

(defn running-week-pairs [dt]
  (->> dt
       at-start-of-week
       (iterate
         #(time/plus
            %
            (time/hours 1)))
       (take
         (inc
           (* 6 24)))
       (map
         #(vector
            (->hour-str %)
            (->hour-str
              (time/plus
                %
                (time/days 1)))))))
