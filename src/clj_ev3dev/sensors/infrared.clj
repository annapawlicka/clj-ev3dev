(ns clj-ev3dev.sensors.infrared
  (:require [clj-ev3dev.devices        :as devices]
            [clj-ev3dev.core           :as core]
            [clj-ev3dev.sensors.common :as common]))

(defn find-infrared-sensor
  "Finds an infrared sensor. If the port is
  provided, it will find the node name with that name.
  If it is not provided, it will use the default
  port in4."
  ([session in-port]
   (devices/find-sensor session "infrared" in-port))
  ([session]
   (devices/find-sensor session "infrared" "in4")))

(defn read-proximity
  "Reads the proximity value (in range 0 - 100)
  reported by the infrared sensor. A value of 100
  corresponds to a range of approximately 70 cm."
  [session sensor]
  (common/read-value session sensor))
