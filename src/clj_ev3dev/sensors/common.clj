(ns clj-ev3dev.sensors.common
  (:require [clj-ev3dev.core :as core]
            [clojure.edn     :as edn]))

(defmulti #^{:private true} command-string (fn [sensor command] (:type command)))
(defmethod command-string :value [sensor _]
  (str "cat /sys/class/msensor/" sensor "/value0"))
(defmethod command-string :mode [sensor command]
  (str "echo \"" (:value command) "\" > /sys/class/msensor/" sensor "/mode"))

(defn read-value
  "Reads current status of the sensor. Returns a numeric value."
  [session sensor]
  (edn/read-string (core/execute session (command-string sensor {:type :value}))))

(defn write-mode
  "Changes the mode of a sensor."
  [session sensor mode]
  (core/execute session (command-string sensor {:type :mode :value mode})))
