(ns clj-ev3dev.sensors.color
  (:require [clj-ev3dev.devices :as devices]))

(def ^{:private true} colors {0 :none
                              1 :black
                              2 :blue
                              3 :green
                              4 :yellow
                              5 :red
                              6 :white
                              7 :brown})

(defn read-color
  "Reads one of seven color values: :none, :black :blue,
  :green, :yellow, :red, :white, :brown.
  Sensor needs to be in a very close proximity to the object,
  e.g. 1cm.

  Sensor passed must be the result of running:
  (find-color-sensor config <in-port>)

  If you're not sure what mode is currently set for the sensor,
  you can check the mode yourself:
  (devices/read-mode config sensor)
  and, if required, update it:
  (devices/set-mode config sensor :col-color)

  Mode is not being checked by default to avoid unnecessary
  roundtrip when e.g. detecting color of the floor while moving."
  [config sensor]
  (get colors (devices/read-value config sensor)))

(defn read-reflected-light-intensity
  "Reads the reflected light intensity in range [0, 100].

  Sensor passed must be the result of running:
  (find-color-sensor session <in-port>)

  If you're not sure what mode is currently set for the sensor,
  you can check the mode yourself:
  (devices/read-mode session sensor)
  and, if required, update it:
  (devices/set-mode session sensor :col-reflect)

  Mode is not being checked by default to avoid unnecessary
  roundtrip when e.g. detecting color of the floor while moving."
  [config sensor]
  (devices/read-value config sensor))

(defn read-ambient-light-intensity
  "Reads the ambient light intensity in range [0, 100].

  Sensor passed must be the result of running:
  (find-color-sensor config <in-port>)

  If you're not sure what mode is currently set for the sensor,
  you can check the mode yourself:
  (devices/read-mode config sensor)
  and, if required, update it:
  (devices/set-mode config sensor :col-ambient)

  Mode is not being checked by default to avoid unnecessary
  roundtrip when e.g. detecting color of the floor while moving."
  [config sensor]
  (devices/read-value config sensor))
