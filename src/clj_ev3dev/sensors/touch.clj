(ns clj-ev3dev.sensors.touch
  (:require [clj-ev3dev.devices  :as devices]
            [clj-ev3dev.core     :as core]))

(defn pressed?
  "Returns true if the touch sensor is pressed.
  Returns false otherwise."
  [session sensor]
  (= 1 (devices/read-value session sensor)))

(defn released?
  "Returns true if the touch sensor is released.
  Returns false otherwise."
  [session sensor]
  (= 0 (devices/read-value session sensor)))
