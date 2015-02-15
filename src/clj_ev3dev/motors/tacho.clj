(ns clj-ev3dev.motors.tacho
  (:require [clj-ev3dev.core    :as core]
            [clj-ev3dev.devices :as devices]
            [clojure.string     :as str]
            [clojure.edn        :as edn]))

(def ^{:private true} motor-api
  "Names of files which constitute the low-level motor API"
  {:root-motor-path   "/sys/class/tacho-motor"
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

(defn- write-state [session motor k value]
  (let [cmd (str "echo " value " > "
                         (:root-motor-path motor-api) "/"
                         motor "/" (k motor-api))]
    (-> (core/execute session cmd)
        edn/read-string)))

(defn- read-state [session motor k]
  (-> (core/execute session (str "cat "
                                 (:root-motor-path motor-api) "/"
                                 motor "/" (k motor-api)))
      edn/read-string))

(defn- out-port-name [motor]
  (str "cat " (:root-motor-path motor-api) "/" motor "/port_name"))

(defn- locate-in-port
  "Searches through motors directory and either returns
  a matching device's node name or returns nil. "
  [session out-port files]
  (first (keep #(let [port (read-state session % :port)]
                  (when (= port (str "out" out-port))
                    %)) files)))

(defn find-tacho-motor
  "Finds tacho motor that is plugged into given port. Ports are: A, B, C, D.
  If no tacho motors are connected, it throws an exception."
  [session out-port]
  (let [motors (str/split-lines (core/execute session (str "ls " (:root-motor-path motor-api))))]
    (if-not (> (count motors) 0)
      (throw (Exception. "There are no tacho motors connected."))
      (locate-in-port session out-port motors))))

(defn write-speed
  "Sets the operating speed of the motor."
  [session motor speed]
  (write-state session motor :speed-write speed))

(defn read-speed
  "Reads the operating speed of the motor."
  [session motor]
  (read-state session motor :speed-read))

(defn write-power
  "Writes the operating power of the motor."
  [session motor power]
  (write-state session motor :power-write power))

(defn read-power
  "Reads the operating power of the motor."
  [session motor]
  (read-state session motor :power-read))

(defn set-duty-cycle
  "Sets the duty cycle. Duty cycle should be a numerical
  value.

  The duty cycle is useful when you just want to turn
  the motor on and are not too concerned with how stable
  the speed is.
  The duty cycle attribute accepts values from -100 to +100.
  The sign of the attribute determines the direction of the motor.
  You can update the duty cycle while the motor is running."
  [session motor value]
  (if (or (> value 100) (< value -100))
    (throw (Exception. "The speed must be in range [-100, 100]."))
    (write-state session motor :duty-cycle-write value)))

(defn read-duty-cycle
  "Reads the duty cycle value."
  [session motor]
  (read-state session motor :duty-cycle-read))

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
  [session motor mode]
  (write-state session motor :regulation-mode (name mode)))

(defn read-regulation-mode
  "Reads the regulation mode of the motor."
  [session motor]
  (read-state session motor :regulation-mode))

(defn enable-break-mode
  "Enables brake mode, causing the motor to brake to stops."
  [session motor]
  (write-state session motor :stop-mode "brake"))

(defn disable-break-mode
  "Disables brake mode, causing the motor to coast to stops.
  Brake mode is off by default."
  [session motor]
  (write-state session motor :stop-mode "coast"))

(defn current-position
  "Reads the current position of the motor."
  [session motor]
  (read-state session motor :position))

(defn initialise-position
  "Set the position of the motor."
  [session motor position]
  (write-state session motor :position position))

(defmulti run-motor
  "Sets the speed of the motor and runs it.
  Depending on the regulation mode it will either set
  pulses per second (enabled) or duty cycle (disabled).

  It throws exception if speed is outside of a valid range."
  (fn [session motor speed regulation-mode] regulation-mode))

(defmethod run-motor :on [session motor speed _]
  (if (and (> speed 2000) (< speed -2000))
    (throw (Exception. "The speed in regulation mode must be in range [-2000, 2000]."))
    (do
      (write-speed session motor speed)
      (write-state session motor :run 1))))

(defmethod run-motor :off [session motor speed _]
  (if (and (> speed 100) (< speed -100))
    (throw (Exception. "The speed must be in range [-100, 100]"))
    (do
      (write-power session motor speed)
      (write-state session motor :run 1))))

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
  [session motor speed]
  (let [regulation-mode (keyword (read-state session motor :regulation-mode))]
    (run-motor session motor speed regulation-mode)))

(defn stop
  "Stops the motor."
  [session motor]
  (write-state session motor :run 0))
