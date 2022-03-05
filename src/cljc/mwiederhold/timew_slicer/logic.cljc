(ns mwiederhold.timew-slicer.logic
  (:require
   [clojure.string :as str]
   [tick.core :as t]))

(defn- instant-of
  "Converts a timew timestamp string to a tick instant"
  [s]
  (t/instant (str (subs s 0 4)
                  "-"
                  (subs s 4 6)
                  "-"
                  (subs s 6 11)
                  ":"
                  (subs s 11 13)
                  ":"
                  (subs s 13))))

(defn- round-time
  "Rounds a tick time to the nearest (e.g. 15min) step"
  [time]
  (let [original-hour (t/hour time)
        rounded-minutes (* (Math/round (float (/ (t/minute time) 15))) 15)]
    (if (= rounded-minutes 60)
      (if (= original-hour 23)
        (t/new-time original-hour 45)
        (t/new-time (+ original-hour 1) 0))
      (t/new-time original-hour rounded-minutes))))

(defn- parse [line]
  (let [start-string (instant-of (subs line 4 20))
        end-string (instant-of (subs line 23 39))]
    {:date (t/date start-string)
     :start-time (t/time start-string)
     :end-time (t/time end-string)
     :tags (subs line 42)}))

(defn- assoc-date-from-first [m]
  (assoc m :date-str (str (:date (first (:tracked-times m))))))

(defn- assoc-start-of-day [m]
  (assoc m :start-of-day (->> (:tracked-times m)
                              (map :start-time)
                              (sort t/<)
                              (first)
                              (round-time))))

(defn- slice-day
  "Slices a day"
  [parsed-times]
  (-> {:tracked-times parsed-times}
      assoc-date-from-first
      assoc-start-of-day))

(defn slice-month
  "Returns all slices for a month, takes and returns tick values"
  [tracked-times]
  [{:date "2022-02-24"
    :slices [{:start "08:00"
              :end "09:00"
              :tags ["VT" "PM"]}
             {:start "09:00"
              :end "10:30"
              :tags ["BF"]}]}
   {:date "2022-02-25"
    :slices [{:start "10:00"
              :end "12:30"
              :tags ["VT"]}
             {:start "09:00"
              :end "10:30"
              :tags ["BF"]}]}])

(comment
  (def times ["inc 20220201T083147Z - 20220201T083653Z # HENNECKE"
              "inc 20220201T083653Z - 20220201T103849Z # VT"
              "inc 20220201T103849Z - 20220201T105140Z # HENNECKE"
              "inc 20220201T105140Z - 20220201T112701Z # VT"
              "inc 20220201T123429Z - 20220201T170949Z # HENNECKE"
              "inc 20220202T073000Z - 20220202T083000Z # VT"
              "inc 20220202T083000Z - 20220202T093000Z # HENNECKE"
              "inc 20220202T093000Z - 20220202T110000Z # \"SAP-Meeting\""
              "inc 20220202T110000Z - 20220202T112707Z # VT"
              "inc 20220202T112707Z - 20220202T114038Z # AIXTRON"
              "inc 20220202T114038Z - 20220202T121630Z # VT"
              "inc 20220202T130930Z - 20220202T132107Z # VT"
              "inc 20220214T080000Z - 20220214T084500Z # Arztbesuch"
              "inc 20220214T090000Z - 20220214T101500Z # VT"
              "inc 20220214T101500Z - 20220214T103000Z # Entwicklermeeting"
              "inc 20220214T103000Z - 20220214T121500Z # VT"
              "inc 20220214T131500Z - 20220214T140000Z # AO"
              "inc 20220214T140000Z - 20220214T150000Z # AIXTRON"
              "inc 20220214T150000Z - 20220214T170314Z # VT"])
  (def single-day-times ["inc 20220201T083653Z - 20220201T103849Z # VT"
                         "inc 20220201T083147Z - 20220201T083653Z # HENNECKE"
                         "inc 20220201T105140Z - 20220201T112701Z # VT"
                         "inc 20220201T103849Z - 20220201T105140Z # HENNECKE"
                         "inc 20220201T123429Z - 20220201T170949Z # HENNECKE"])
  (parse (first times))
  (slice-month nil)
  (clojure.pprint/pprint (slice-day (map parse single-day-times)))
  [{:date-str "2022-02-01"
    :start-of-day (t/new-time 8 30)
    :slices [{:duration (t/new-duration (* 60 60 3) :seconds)
              :start (t/new-time 8 30)
              :start-str "08:30"
              :end (t/new-time 11 30)
              :end-str "11:30"
              :tags ["HENNECKE"]}
             {:duration (t/new-duration (* 60 60 2) :seconds)
              :start (t/new-time 12 30)
              :start-str "12:30"
              :end (t/new-time 14 30)
              :end-str "14:30"
              :tags ["HENNECKE"]}
             {:duration (t/new-duration (* 60 150) :seconds)
              :start (t/new-time 14 30)
              :start-str "14:30"
              :end (t/new-time 17 0)
              :end-str "17:00"
              :tags ["VT"]}]
    ;; :breaks [{:duration (t/new-duration (* 60 60) :seconds)
    ;;           :start (t/new-time 11 30)
    ;;           :end (t/new-time 12 30)}]
    :breaks []
    ;; :available [{:start (t/new-time 8 30)
    ;;              :end (t/new-time 11 30)}
    ;;             {:start (t/new-time 12 30)
    ;;              :end (t/new-time 17 0)}]
    :available []
    ;; :totals [{:tags ["HENNECKE"]
    ;;           :duration (t/new-duration (* 60 60 5) :seconds)}
    ;;          {:tags ["VT"]
    ;;           :duration (t/new-duration (* 60 150) :seconds)}]
    :totals [{:tags ["HENNECKE"]
              :duration (t/new-duration 0 :seconds)}
             {:tags ["VT"]
              :duration (t/new-duration 0 :seconds)}]}])