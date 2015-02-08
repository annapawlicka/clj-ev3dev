(ns clj-ev3dev.sensors.color
  (:require [clj-ev3dev.sensors        :as sensors]
            [clj-ev3dev.core           :as core]
            [clj-ev3dev.sensors.common :as common]))

(def colors {0 :none
             1 :black
             2 :blue
             3 :green
             4 :yellow
             5 :red
             6 :white
             7 :brown})

(defn find-color-sensor
  ([session in-port]
   (sensors/find-sensor session "color" in-port))
  ([session]
   (sensors/find-sensor session "color" "in3")))

(defn toggle-mode
  "Sensor can act in three different modes: COL-COLOR,
  COL-REFLECT, COL-AMBIENT. To toggle the color we need to
  write into a file, which requires sudo permissions. If you're
  using root user, you don't have to do anything. For any other user
  please make sure they have neccessary permissions to do `echo` to
  a file."
  [session sensor mode]
  (common/write-mode session sensor mode))

(defn read-color
  "Reads one of seven color values.
  Sensor needs to be in a very close proximity to the object,
  e.g. 1cm."
  [session sensor]
  (toggle-mode session sensor "COL-COLOR")
  (get colors (common/read-value session sensor)))

(defn read-reflected-light-intensity
  "Reads the reflected light intensity in range [0, 100]."
  [session sensor]
  (toggle-mode session sensor "COL-REFLECT")
  (common/read-value session sensor))

(defn read-ambient-light-intensity
  "Reads the ambient light intensity in range [0, 100]."
  [session sensor]
  (toggle-mode session sensor "COL-AMBIENT")
  (common/read-value session sensor))
