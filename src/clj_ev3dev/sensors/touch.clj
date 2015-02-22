(ns clj-ev3dev.sensors.touch
  (:require [clj-ev3dev.devices :as devices]))

(defn pressed?
  "Returns true if the touch sensor is pressed.
  Returns false otherwise."
  [sensor]
  (= 1 (devices/read-value sensor)))

(defn released?
  "Returns true if the touch sensor is released.
  Returns false otherwise."
  [sensor]
  (= 0 (devices/read-value sensor)))
