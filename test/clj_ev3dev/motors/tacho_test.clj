(ns clj-ev3dev.motors.tacho-test
  (:require [clojure.test            :refer :all]
            [clj-ev3dev.motors.tacho :refer :all]))


(deftest read-state-test
  (with-redefs [motor-api {:root-motor-path "resources/test/tacho-motor/"
                           :port            "port_name"}]
    (testing "Testing read-state."
      (is (= "outB"
             (read-state {:env :local} "motor0" :port))))))
