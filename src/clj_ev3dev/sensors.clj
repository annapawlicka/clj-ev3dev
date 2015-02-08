(ns clj-ev3dev.sensors
  (:require [clj-ev3dev.core :as core]
            [clojure.string  :as str]))

(def type-resolver {"touch"    "lego-ev3-touch"
                    "color"    "lego-ev3-uart-29"
                    "infrared" "ev3-uart-33"})

(defn port-name [sensor]
  (str "cat /sys/class/msensor/" sensor "/port_name"))

(defn type-name [sensor]
  (str "cat /sys/class/msensor/" sensor "/name"))

(defn locate-in-port
  "Searches through sensors directory and either returns
  a matching sensor's node name or returns nil."
  [session sensor-type in-port files]
  (first (keep #(let [port (core/execute session (port-name %))
                      typ  (core/execute session (type-name %))]
                  (when (and (= in-port port)
                             (= (get type-resolver sensor-type) typ))
                    %)) files)))

(defn find-sensor
  "Finds sensor's node name by searching for
  sensor type and port that it's plugged in."
  [session sensor-type in-port]
  (let [files  (str/split-lines (core/execute session "ls /sys/class/msensor"))]
    (locate-in-port session sensor-type in-port files)))
