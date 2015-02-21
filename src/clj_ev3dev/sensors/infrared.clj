(ns clj-ev3dev.sensors.infrared
  (:require [clj-ev3dev.devices  :as devices]
            [clj-ev3dev.core     :as core]))

(defn read-proximity
  "Reads the proximity value (in range 0 - 100)
  reported by the infrared sensor. A value of 100
  corresponds to a range of approximately 70 cm.

  If you're not sure in which mode the sensor currently
  operates, you can check it by running:
  (devices/read-mode session sensor)

  To change the mode of the sensor to one of the followng:
  :ir-prox   Proximity
  :ir-seek   IR seeker
  :ir-remote IR remote control (button)
  :ir-rem-a  IR remote control
  :ir-s-alt  Alternate IR seeker,

  please run:
  (devices/write-mode session sensor :ir-prox)"
  [session sensor]
  (devices/read-value session sensor))
