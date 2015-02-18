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
