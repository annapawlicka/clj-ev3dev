(ns clj-ev3dev.motors.led
  (:require [clj-ev3dev.core :as core]
            [clojure.edn     :as edn]))

(defn read-intensity
  "Reads the current brigtness of the led.
  Led passed has to be the result of
  running:
  clj-ev3dev.devices/find-led

   Returns a numeric value."
  [session led]
  (let [cmd (str "cat /sys/class/leds/" led "/brightness")]
    (edn/read-string (core/execute session cmd))))

(defn max-intensity
  "Reads maximum brightness of the led.
  Led passed has to be the result of
  running:
  clj-ev3dev.devices/find-led

  Returns a numeric value.
  "
  [session led]
  (let [cmd (str "cat /sys/class/leds/" led "/max_brightness")]
    (edn/read-string (core/execute session cmd))))

(defn set-intensity
  "Sets the brightness of the led.
  Led passed has to be the result of
  running:
  clj-ev3dev.devices/find-led

  Intensity value should not exceed
  the maximum value for the led."
  [session led intensity]
  (let [cmd (str "echo " intensity " > /sys/class/leds/" led "/brightness")]
    (core/execute session cmd)))
