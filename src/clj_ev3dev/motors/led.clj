(ns clj-ev3dev.motors.led
  (:require [clj-ev3dev.core :as core]
            [clojure.edn     :as edn]))

(defn read-intensity
  "Reads the current brigtness of the led. "
  [session led]
  (let [cmd (str "cat /sys/class/leds/" led "/brightness")]
    (edn/read-string (core/execute session cmd))))

(defn max-intensity
  "Reads maximum brightness of the led."
  [session led]
  (let [cmd (str "cat /sys/class/leds/" led "/max_brightness")]
    (edn/read-string (core/execute session cmd))))

(defn set-intensity
  "Sets the brightness of the led."
  [session led intensity]
  (let [cmd (str "echo " intensity " > /sys/class/leds/" led "/brightness")]
    (core/execute session cmd)))
