(ns clj-ev3dev.devices
  (:require [clj-ev3dev.core :as core]
            [clojure.string  :as str]))

(def type-resolver {:touch       "lego-ev3-touch"
                    :color       "ev3-uart-29"
                    :infrared    "ev3-uart-33"
                    :tacho       "tacho"
                    :green-left  "ev3:green:left"
                    :green-right "ev3:green:right"
                    :red-left    "ev3:red:left"
                    :red-right   "ev3:red:right"})

(defn port-name [sensor]
  (str "cat /sys/class/msensor/" sensor "/port_name"))

(defn type-name [sensor]
  (str "cat /sys/class/msensor/" sensor "/name"))

(defn locate-in-port
  "Searches through devices directory and either returns
  a matching device's node name or returns nil."
  [session device-type in-port files]
  (first (keep #(let [port  (core/execute session (port-name %))
                      typ   (core/execute session (type-name %))
                      class (get type-resolver device-type)]
                  (when (and (= in-port port)
                             (= class typ))
                    %)) files)))

(defn- find-device
  [session cmd device-type in-port]
  (let [files (str/split-lines (core/execute session cmd))]
    (locate-in-port session device-type in-port files)))

(defn find-sensor
  "Finds sensor's node name by searching for
  sensor type and port that it's plugged in."
  [session sensor-type in-port]
  (find-device session "ls /sys/class/msensor" sensor-type in-port))

(defn find-tacho-motor
  "Finds tacho motor's node name by searching for
  motor type and port that it's plugged in."
  [session motor-type in-port]
  (find-device session "ls /sys/class/tacho-motor" motor-type in-port))

(defn find-led
  "Returns node name for a given led color and side.
  Leds are static (they don't change their location on
  the brick."
  [led-type]
  (get type-resolver led-type))
