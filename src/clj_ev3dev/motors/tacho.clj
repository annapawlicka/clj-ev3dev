(ns clj-ev3dev.motors.tacho
  (:require [clj-ev3dev.devices :as devices]
            [clojure.string     :as str]
            [clojure.java.shell :as shell]
            [clj-ev3dev.ssh  :as ssh]))

(def motor-api
  "Names of files which constitute the low-level motor API"
  {:root-motor-path   "/sys/class/tacho-motor/"
   :port              "port_name"
   :regulation-mode   "regulation_mode"
   :speed-read        "pulses_per_second"
   :speed-write       "pulses_per_second_sp"
   :power-read        "duty_cycle"
   :power-write       "duty_cycle_sp"
   :run               "run"
   :stop-mode         "stop_mode"
   :position          "position"
   :duty-cycle-read   "duty_cycle"
   :duty-cycle-write  "duty_cycle_sp"})

(defn- str->num [s]
  (. Integer parseInt s))

(defmulti  write-state (fn [config _ _ _] (:env config)))

(defmethod write-state :remote [config motor k value]
  (let [cmd (str "echo " value " > "
                         (:root-motor-path motor-api) "/"
                         motor "/" (k motor-api))]
    (ssh/execute (:session config) cmd)))

(defmethod write-state :local [config motor k value]
  (spit (str (:root-motor-path motor-api) motor "/" (k motor-api)) value))

(defmulti read-state (fn [config _ _] (:env config)))

(defmethod read-state :remote [config motor k]
  (ssh/execute (:session config) (str "cat "
                                      (:root-motor-path motor-api) "/"
                                      motor "/" (k motor-api))))

(defmethod read-state :local [_ motor k]
  (let [path (str (:root-motor-path motor-api) motor "/" (k motor-api))]
    (str/trim-newline (slurp path))))

(defn- locate-in-port
  "Searches through motors directory and either returns
  a matching device's node name or returns nil."
  ([config out-port files]
   (first (keep #(let [port (read-state (:session config) % :port)]
                   (when (= port out-port)
                     %)) files)))
  ([out-port motors]
   (first (keep #(let [port (read-state % :port)]
                   (when (= out-port port)
                     %)) motors))))

(defmulti find-tacho-motor
  "Finds tacho motor that is plugged into given port. Ports are: :a, :b, :c, :d."
  (fn [config _] (:env config)))

(defmethod find-tacho-motor :remote [config port]
  (let [motors (str/split-lines (ssh/execute (:session config) (str "ls " (:root-motor-path motor-api))))]
    (if-not (> (count motors) 0)
      (throw (Exception. "There are no tacho motors connected."))
      (locate-in-port (:session config) (get devices/ports port) motors))))

(defmethod find-tacho-motor :local [config port]
  (let [motors (str/split-lines (:out (clojure.java.shell/sh "ls" (:root-motor-path motor-api))))]
    (locate-in-port (get devices/ports port) motors)))

(defn write-speed
  "Sets the operating speed of the motor."
  [config motor speed]
  (write-state config motor :speed-write speed))

(defn read-speed
  "Reads the operating speed of the motor.
  Returns numerical value."
  [config motor]
  (str->num (read-state config motor :speed-read)))

(defn write-power
  "Writes the operating power of the motor."
  [config motor power]
  (write-state config motor :power-write power))

(defn read-power
  "Reads the operating power of the motor.
  Returns numerical value."
  [config motor]
  (str->num (read-state config motor :power-read)))

(defn set-duty-cycle
  "Sets the duty cycle. Duty cycle should be a numerical
  value.

  The duty cycle is useful when you just want to turn
  the motor on and are not too concerned with how stable
  the speed is.
  The duty cycle attribute accepts values from -100 to +100.
  The sign of the attribute determines the direction of the motor.
  You can update the duty cycle while the motor is running."
  [config motor value]
  (if (or (> value 100) (< value -100))
    (throw (Exception. "The speed must be in range [-100, 100]."))
    (write-state config motor :duty-cycle-write value)))

(defn read-duty-cycle
  "Reads the duty cycle value. Returns a numerical value."
  [config motor]
  (str->num (read-state config motor :duty-cycle-read)))

(defn set-regulation-mode
  "Toggle regulation mode of the motor to :off or :on.
  Default mode is :off.

  :off mode
  In this mode, the motor driver uses the duty cycle
  to determine what percentage of the battery voltage
  to send to the motor.
  If you run the motor at a fairly low duty cycle, and
  you try to stop the hub of the motor with your thumb,
  you'll find it's pretty easy to slow down or even stop
  the motor. In some cases, this is not what you want. You
  want the motor to 'try harder' when it encounters
  resistanceand - and the regulation_mode attribute is
  going to help us with that.

  :on mode
  When the regulation_mode attribute is set to on, the motor
  driver attempts to keep the motor speed at the value you've
  specified in pulses_per_second_sp. If you slow down the motor
  with a load, the motor driver tries to compensate by sending
  more power to the motor to get it to speed up. If you speed up
  the motor for some reason, the motor driver will try to compensate
  by sending less power to the motor."
  [config motor mode]
  (write-state config motor :regulation-mode (name mode)))

(defn read-regulation-mode
  "Reads the regulation mode of the motor."
  [config motor]
  (read-state config motor :regulation-mode))

(defn enable-break-mode
  "Enables brake mode, causing the motor to brake to stops."
  [config motor]
  (write-state config motor :stop-mode "brake"))

(defn disable-break-mode
  "Disables brake mode, causing the motor to coast to stops.
  Brake mode is off by default."
  [config motor]
  (write-state config motor :stop-mode "coast"))

(defn current-position
  "Reads the current position of the motor.
  Returns numerical value."
  [config motor]
  (str->num (read-state config motor :position)))

(defn initialise-position
  "Set the position of the motor."
  [config motor position]
  (write-state config motor :position position))

(defmulti run-motor
  "Sets the speed of the motor and runs it.
  Depending on the regulation mode it will either set
  pulses per second (enabled) or duty cycle (disabled).

  It throws exception if speed is outside of a valid range."
  (fn [config motor speed regulation-mode] regulation-mode))

(defmethod run-motor :on [config motor speed _]
  (if (and (> speed 2000) (< speed -2000))
    (throw (Exception. "The speed in regulation mode must be in range [-2000, 2000]."))
    (do
      (write-speed config motor speed)
      (write-state config motor :run 1))))

(defmethod run-motor :off [config motor speed _]
  (if (and (> speed 100) (< speed -100))
    (throw (Exception. "The speed must be in range [-100, 100]"))
    (do
      (write-power config motor speed)
      (write-state config motor :run 1))))

(defn run
  "Runs the motor at the given port.
  The meaning of `speed` parameter depends on whether the
  regulation mode is turned on or off.
  When the regulation mode is off (by default) `speed` ranges
  from -100 to 100 and it's absolute value indicates the percent
  of motor's power usage. It can be roughly interpreted as a motor
  speed, but deepending on the environment, the actual speed of the
  motor may be lower than the target speed.

  When the regulation mode is on (has to be enabled by enable-regulation-mode
  function) the motor driver attempts to keep the motor speed at the `speed`
  value you've specified which ranges from -2000 to 2000.

  Negative values indicate reverse motion regardless of the regulation mode."
  [config motor speed]
  (let [regulation-mode (keyword (read-state motor :regulation-mode))]
    (run-motor config motor speed regulation-mode)))

(defn stop
  "Stops the motor."
  [config motor]
  (write-state config motor :run 0))
