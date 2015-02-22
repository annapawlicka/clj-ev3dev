(ns clj-ev3dev.devices-test
  (:require [clojure.test       :refer :all]
            [clj-ev3dev.devices :refer :all]))

(def path "resources/test/lego-sensor/")

(deftest locate-in-port-test
  (with-redefs [sensor-path path]
    (testing "Testing locate-in-port"
      (is (= "sensor1"
             (locate-in-port :color "in2")))
      (is (= "sensor2"
             (locate-in-port :touch "in1")))
      (nil? (locate-in-port :infrared "in1")))))

(deftest write-mode-test
  (with-redefs [sensor-path path]
    (testing "Testing write-mode"
      (is (= :col-color
             (do
               (write-mode {:type :color :node "sensor1"} :col-color)
               (read-mode {:type :color :node "sensor1"})))))))

(deftest read-mode-test
  (with-redefs [sensor-path path]
    (testing "Testing read-mode"
      (is (= :col-ambient
             (do
               (write-mode {:type :color :node "sensor1"} :col-ambient)
               (read-mode {:type :color :node "sensor1"})))))))

(deftest read-units-test
  (with-redefs [sensor-path path]
    (testing "Testing read-units."
      (is (= :pct
             (read-units {:type :infrared :node "sensor3"}))))))

(deftest read-driver-name-test
  (with-redefs [sensor-path path]
    (testing "Testing read-driver-name."
      (is (= "ev3-uart-29"
             (read-driver-name {:type :infrared :node "sensor1"}))))))

(deftest read-port-name-test
  (with-redefs [sensor-path path]
    (testing "Testing read-port-name."
      (is (= "in2"
             (read-port-name {:type :infrared :node "sensor1"}))))))

(deftest read-value-test
  (with-redefs [sensor-path path]
    (testing "Testing read-value."
      (is (= 22
             (read-value {:type :infrared :node "sensor1"}))))))

(deftest find-sensor-test
  (with-redefs [sensor-path path]
    (testing "Testing find-sensor."
      (is (= "sensor1"
             (:node (find-sensor :color :2))))
      (is (= "sensor2"
             (:node (find-sensor :touch)))))))

(deftest find-led-test
  (testing "Testing find-led."
    (is (= "ev3:green:right"
           (:node (find-led :green-right))))))
