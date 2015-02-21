(ns clj-ev3dev.devices
  (:require [clj-ev3dev.core :as core]
            [clojure.string  :as str]))

(def type-resolver {:touch       "lego-ev3-touch"
                    :color       "ev3-uart-29"
                    :infrared    "ev3-uart-33"
                    :green-left  "ev3:green:left"
                    :green-right "ev3:green:right"
                    :red-left    "ev3:red:left"
                    :red-right   "ev3:red:right"})

(def default-ports {:touch    "in1"
                    :color    "in3"
                    :infrared "in4"})

(def ports {:a "outA"
            :b "outB"
            :c "outC"
            :d "outD"
            :1 "in1"
            :2 "in2"
            :3 "in3"
            :4 "in4"})

(def modes {:color    #{:col-color :col-ambient :col-reflect}
            :infrared #{:ir-prox :ir-seek :ir-remote :ir-rem-a :ir-s-alt}})

(defn- str->keyword
  "Converts a string to lower case keyword with
  all spaces repalced with underscores."
  [s]
  (-> s
      (str/replace #" " "_")
      (str/lower-case)
      keyword))

(defn- keyword->str
  "Converts keyqord to upper-case string."
  [k]
  (-> k
      name
      str/upper-case))

(defmulti #^{:private true} command-string (fn [sensor command] (:type command)))

(defmethod command-string :read-value [{:keys [node]} _]
  (str "cat /sys/class/msensor/" node "/value0"))

(defmethod command-string :read-mode [{:keys [node]} _]
  (str "cat /sys/class/msensor/" node "/mode"))

(defmethod command-string :read-units [{:keys [node]} _]
  (str "cat /sys/class/msensor/" node "/units"))

(defmethod command-string :write-value [{:keys [node]} command]
  (str "echo \"" (:value command) "\" > /sys/class/msensor/" node "/value"))

(defmethod command-string :write-mode [{:keys [node]} command]
  (str "echo \"" (:value command) "\" > /sys/class/msensor/" node "/mode"))

(defn read-value
  "Reads current status of the sensor. Returns a numeric value."
  [session sensor]
  (let [v (core/execute session (command-string sensor {:type :read-value}))]
    (when-not (empty? v)
      (. Integer parseInt v))))

(defn read-mode
  "Reads current mode of the sensor. Returns a keyword."
  [session sensor]
  (str->keyword (core/execute session (command-string sensor {:type :read-mode}))))

(defn valid-mode? [sensor mode]
  (contains? (get modes (:type sensor)) mode))

(defn write-mode
  "Changes the mode of a sensor."
  [session sensor mode]
  (if (valid-mode? sensor mode)
    (core/execute session (command-string sensor {:type :write-mode :value (keyword->str mode)}))
    (throw (Exception. "Please provide a valid mode for this sensor."))))

(defn read-units
  "Returns the units in which the sensor oeprates."
  [session sensor]
  (str->keyword (core/execute session (command-string sensor {:type :read-units}))))

(defn- port-name [sensor]
  (str "cat /sys/class/msensor/" sensor "/port_name"))

(defn- type-name [sensor]
  (str "cat /sys/class/msensor/" sensor "/name"))

(defn- locate-in-port
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
  [session cmd device-type port]
  (let [files (str/split-lines (core/execute session cmd))
        port  (if port (get ports port) (get default-ports device-type))]
    {:type device-type :node (locate-in-port session device-type port files)}))

(defn find-sensor
  "Finds sensor's node name by searching for
  sensor type and port that it's plugged in.

  Sensor types: :touch, :color, :infrared.
  Ports are: :1, :2, :3, :4."
  [session sensor-type & [port]]
  (let [device (find-device session "ls /sys/class/msensor" sensor-type port)]
    (if (:node device)
      device
      (throw (Exception. "Could not locate the device. Please check the ports.")))))

(defn find-led
  "Returns node name for a given led color and side.
  Leds are static (they don't change their location on
  the brick) so it always returns the same mapping.

  Led types: :green-left, :green-right, :red-left, :red-right."
  [led-type]
  {:type led-type :node (get type-resolver led-type)})
