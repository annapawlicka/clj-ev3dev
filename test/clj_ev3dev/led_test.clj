(ns clj-ev3dev.led-test
  (:require [clojure.test   :refer :all]
            [clj-ev3dev.led :refer :all]))

(deftest find-mode-test
  (testing "Testing find-mode"
    (is (= :none (find-mode "[none] mmc0 timer heartbeat default-on transient legoev3-battery-charging-or-full legoev3-battery-charging legoev3-battery-full legoev3-battery-charging-blink-full-solid rfkill0")))
    (is (= :default-on (find-mode "none mmc0 timer heartbeat [default-on] transient legoev3-battery-charging-or-full legoev3-battery-charging legoev3-battery-full legoev3-battery-charging-blink-full-solid rfkill0")))
    (is (= :legoev3-battery-charging-blink-full-solid
           (find-mode "none mmc0 timer heartbeat default-on transient legoev3-battery-charging-or-full legoev3-battery-charging legoev3-battery-full [legoev3-battery-charging-blink-full-solid] rfkill0")))))
