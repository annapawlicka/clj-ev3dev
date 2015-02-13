(ns clj-ev3dev.sensors.touch
  (:require [clj-ev3dev.devices        :as devices]
            [clj-ev3dev.core           :as core]
            [clj-ev3dev.sensors.common :as common]))

(defn pressed?
  "Returns true if the touch sensor is pressed.
  Returns false otherwise."
  [session sensor]
  (= 1 (common/read-value session sensor)))

(defn released?
  "Returns true if the touch sensor is released.
  Returns false otherwise."
  [session sensor]
  (= 0 (common/read-value session sensor)))

(defn find-touch-sensor
  "Finds a touch sensor. If the port is provided,
  it will try and find the node name with that port.
  If the port is not provided, it will use the default
  port in1."
  ([session in-port]
   (devices/find-sensor session "touch" in-port))
  ([session]
   (devices/find-sensor session "touch" "in1")))
