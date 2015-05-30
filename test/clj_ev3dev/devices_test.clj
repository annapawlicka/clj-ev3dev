(ns clj-ev3dev.devices-test
  (:require [clojure.test       :refer :all]
            [clj-ev3dev.devices :refer :all]))

(def path "resources/test/lego-sensor/")

(def test-paths {:touch       "resources/test/lego-sensor/"
                 :color       "resources/test/lego-sensor/"
                 :infrared    "resources/test/lego-sensor/"
                 :red-left    "resources/test/leds/"
                 :red-right   "resources/test/leds/"
                 :green-left  "resources/test/leds/"
                 :green-right "resources/test/leds/"})

(deftest locate-in-port-test
  (with-redefs [paths test-paths]
    (testing "Testing locate-in-port"
      (let [config {:env :local}]
        (is (= "sensor1"
               (locate-in-port config {:device-type :color :port "in2"})))
        (is (= "sensor2"
               (locate-in-port config {:device-type :touch :port "in1"})))
        (nil? (locate-in-port config {:device-type :infrared :port "in1"}))))))

(deftest write-mode-test
  (with-redefs [paths test-paths]
    (testing "Testing write-mode"
      (let [config {:env :local}]
        (is (= :col-color
               (do
                 (write-mode config {:device-type :color :node "sensor1"} :col-color)
                 (read-mode config {:device-type :color :node "sensor1"}))))))))

(deftest read-mode-test
  (with-redefs [paths test-paths]
    (testing "Testing read-mode"
      (let [config {:env :local}]
        (is (= :col-ambient
               (do
                 (write-mode config {:device-type :color :node "sensor1"} :col-ambient)
                 (read-mode config {:device-type :color :node "sensor1"}))))))))

(deftest read-units-test
  (with-redefs [paths test-paths]
    (testing "Testing read-units."
      (is (= :pct
             (read-units {:env :local} {:device-type :infrared :node "sensor3"}))))))

(deftest read-driver-name-test
  (with-redefs [paths test-paths]
    (testing "Testing read-driver-name."
      (is (= "lego-ev3-color"
             (read-driver-name {:env :local} {:device-type :infrared :node "sensor1"}))))))

(deftest read-port-name-test
  (with-redefs [paths test-paths]
    (testing "Testing read-port-name."
      (is (= "in2"
             (read-port-name {:env :local} {:device-type :infrared :node "sensor1"}))))))

(deftest read-value-test
  (with-redefs [paths test-paths]
    (testing "Testing read-value."
      (is (= 22
             (read-value {:env :local} {:device-type :infrared :node "sensor1"}))))))

(deftest find-sensor-test
  (with-redefs [paths test-paths]
    (testing "Testing find-sensor."
      (let [config {:env :local}]
        (is (= "sensor1"
               (:node (find-sensor config {:device-type :color :port :two}))))
        (is (= "sensor2"
               (:node (find-sensor config {:device-type :touch :port :one}))))))))

(deftest find-led-test
  (testing "Testing find-led."
    (is (= "ev3:green:right"
           (:node (find-led :green-right))))))
