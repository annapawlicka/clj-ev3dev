(ns clj-ev3dev.devices
  (:require [clojure.string  :as str]
            [clojure.java.io :as io]))

(def sensor-path "/sys/class/lego-sensor/")

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helpers

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

(defn- build-path
  "Creates a path to an attribute k, where k
  is a keyword.
  Returns a string."
  ([sensor k]
   (str sensor-path (:node sensor) "/" (name k)))
  ([path sensor k]
   (str path (:node sensor) "/" (name k))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Attributes

(defn write-attr
  "Writes new value for a given attribute."
  ([sensor attr value]
   (spit (str sensor-path (:node sensor) "/" (name attr)) value))
  ([path sensor attr value]
   (spit (str path (:node sensor) "/" (name attr)) value)))

(defn read-attr
  "Reads in file of an attribute attr and trims new line."
  ([sensor attr]
   (str/trim-newline (slurp (build-path sensor attr))))
  ([path sensor attr]
   (str/trim-newline (slurp (build-path path sensor attr)))))

(defn read-port-name [sensor]
  (read-attr sensor :port_name))

(defn read-driver-name [sensor]
  (read-attr sensor :driver_name))

(defn read-value
  "Reads current status of the sensor. Returns a numeric value."
  [sensor]
  (let [v (read-attr sensor :value0)]
    (when-not (empty? v)
      (. Integer parseInt v))))

(defn read-mode
  "Reads current mode of the sensor. Returns a keyword."
  [sensor]
  (str->keyword (read-attr sensor :mode)))

(defn read-units
  "Returns the units in which the sensor oeprates."
  [sensor]
  (str->keyword (read-attr sensor :units)))

(defn- valid-mode? [sensor mode]
  (contains? (get modes (:type sensor)) mode))

(defn write-mode
  "Changes the mode of a sensor."
  [sensor mode]
  (if (valid-mode? sensor mode)
    (write-attr sensor :mode (keyword->str mode))
    (throw (Exception. "Please provide a valid mode for this sensor."))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Mapping

(defn locate-in-port
  "Searches through devices directory and either returns
  a matching device's node name or returns nil."
  [device-type in-port]
  (let [dir (io/file sensor-path)]
    (first (keep #(when (.isDirectory %)
                    (let [n (str/trim-newline (.getName %))]
                      (when (.startsWith n "sensor")
                        (let [port  (read-port-name {:node n})
                              typ   (read-driver-name {:node n})
                              class (get type-resolver device-type)]
                          (when (and (= in-port port)
                                     (= class typ))
                            n))))) (file-seq dir)))))

(defn- find-device
  [device-type port]
  (let [port  (if port (get ports port) (get default-ports device-type))]
    {:type device-type :node (locate-in-port device-type port)}))

(defn find-sensor
  "Finds sensor's node name by searching for
  sensor type and port that it's plugged in.

  Sensor types: :touch, :color, :infrared.
  Ports are: :1, :2, :3, :4."
  [sensor-type & [port]]
  (let [device (find-device sensor-type port)]
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
