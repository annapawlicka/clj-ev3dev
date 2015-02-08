(ns clj-ev3dev.sensors.touch
  (:require [clj-ev3dev.sensors        :as sensors]
            [clj-ev3dev.core           :as core]
            [clj-ev3dev.sensors.common :as common]))

(defn pressed? [session sensor]
  (= 1 (common/read-value session sensor)))

(defn released? [session sensor]
  (= 0 (common/read-value session sensor)))

(defn find-touch-sensor
  ([session in-port]
   (sensors/find-sensor session "touch" in-port))
  ([session]
   (sensors/find-sensor session "touch" "in1")))
