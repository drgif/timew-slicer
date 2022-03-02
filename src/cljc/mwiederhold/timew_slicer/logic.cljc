(ns mwiederhold.timew-slicer.logic
  (:require
   [tick.core :as t]))

(defn month-slices
  "Returns all slices for a month, takes and returns tick values"
  [year-month tracked-times]
  [{:date (t/new-date 2022 2 24)
    :slices [{:start (t/new-time 8 0)
              :end (t/new-time 9 0)
              :tags ["VT"]}
             {:start (t/new-time 9 0)
              :end (t/new-time 10 30)
              :tags ["BF"]}]}
   {:date (t/new-date 2022 2 25)
    :slices [{:start (t/new-time 10 0)
              :end (t/new-time 12 30)
              :tags ["VT"]}
             {:start (t/new-time 9 0)
              :end (t/new-time 10 30)
              :tags ["BF"]}]}])

(comment
  (month-slices nil nil))