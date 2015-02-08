(ns clj-ev3dev.sensors.infrared
  (:require [clj-ev3dev.sensors :as sensors]
            [clj-ev3dev.core    :as core]))

;; TODO put all path builders into multimethod in a common ns
(defn value [sensor]
  (str "cat /sys/class/msensor/" sensor "/value0"))

(defn read-proximity [session]
  (let [sensor (sensors/find-sensor session "touch" "in1")] ;; TODO make configurable
    (core/execute session (value sensor))))
