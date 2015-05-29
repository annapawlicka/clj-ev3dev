(ns clj-ev3dev.motors.tacho
  (:require [clj-ev3dev.devices :as devices]
            [clojure.string     :as str]
            [clojure.java.shell :as shell]
            [clj-ev3dev.ssh  :as ssh]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Helpers

(def root-motor-path "/sys/class/tacho-motor/")

(defn keyword->attr
  "Converts keyword to a file name."
  [k]
  (-> k
      name
      (str/replace #"-" "_")))

(defn- str->num [s]
  (when-not (empty? s)
    (. Integer parseInt s)))

(defmulti send-command (fn [config _ _] (:env config)))

(defmethod send-command :remote [config motor value]
  (let [cmd (str "echo " (name value) " > "
                 root-motor-path
                 motor "/command")]
    (ssh/execute (:session config) cmd)))

(defmethod send-command :local [config motor value]
  (spit (str root-motor-path "/"
             motor "/command")) (name value))

(defmulti  write-attr (fn [config _ _ _] (:env config)))

(defmethod write-attr :remote [config motor k value]
  (let [cmd (str "echo " value " > " root-motor-path
                 motor "/" (keyword->attr k))]
    (ssh/execute (:session config) cmd)))

(defmethod write-attr :local [config motor k value]
  (spit (str root-motor-path motor "/" (keyword->attr k)) (keyword->attr value)))

(defmulti read-attr (fn [config _ _] (:env config)))

(defmethod read-attr :remote [config motor k]
  (let [cmd (str "cat " root-motor-path "/"
                 motor "/" (keyword->attr k))]
    (ssh/execute (:session config) cmd)))

(defmethod read-attr :local [_ motor k]
  (let [path (str root-motor-path motor "/" (keyword->attr k))]
    (str/trim-newline (slurp path))))

(defn- locate-in-port
  "Searches through motors directory and either returns
  a matching device's node name or returns nil."
  [config out-port files]
  {:post [(or (not (empty? %))
              (throw (Exception. (format "Could not locate motor in port %s" out-port))))]}
  (first (keep #(let [port (read-attr config % :port-name)]
                  (when (= port out-port)
                    %)) files)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Commands

(defn reset
  "Reset all of the parameters back to the default values and stop the motor.

  It's a good idea to send this command at the start of a program to ensure the motor
  is in a known state without having to write each of the parameters individually."
  [config motor]
  (send-command config motor :reset))

(defn current-position
  "Reads the current position of the motor.
  Returns numerical value."
  [config motor]
  (str->num (read-attr config motor :position)))

(defn reset-position
  "Resets the position of the motor to 0.

  NOTE: You will get an error if you do this while the motor is running."
  [config motor]
  (write-attr config motor :position 0))

(defn initialise-position
  "Set the position of the motor."
  [config motor position]
  (write-attr config motor :position position))


;;;;;; Locating motor ;;;;;;;;

(defmulti find-tacho-motor
  "Finds tacho motor that is plugged into given port. Ports are: :a, :b, :c, :d."
  (fn [config _] (:env config)))

(defmethod find-tacho-motor :remote [config port]
  (let [motors (str/split-lines (ssh/execute (:session config) (str "ls " root-motor-path)))]
    (if-not (> (count motors) 0)
      (throw (Exception. "There are no tacho motors connected."))
      (locate-in-port config (get devices/ports port) motors))))

(defmethod find-tacho-motor :local [config port]
  (let [motors (str/split-lines (:out (clojure.java.shell/sh "ls" root-motor-path)))]
    (locate-in-port config (get devices/ports port) motors)))

(defn write-duty-cycle
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
    (write-attr config motor :duty-cycle-sp value)))

(defn read-duty-cycle
  "Reads the duty cycle value. Returns a numerical value."
  [config motor]
  (str->num (read-attr config motor :duty-cycle)))

(defn set-speed
  "Sets the speed value of a motor, in tacho counts per second.
  It's used with regulation mode on."
  [config motor speed]
  (write-attr config motor :speed-sp speed))

(defn read-speed
  "Reads the speed value. Returns a numerical value."
  [config motor]
  (str->num (read-attr config motor :speed-sp)))

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
  specified.
  You can change the speed by calling set-speed function, e.g.
  (set-speed config motor 275)

  If you slow down the motor with a load, the motor driver tries
  to compensate by sending more power to the motor to get it to
  speed up. If you speed up the motor for some reason, the motor
  driver will try to compensate by sending less power to the motor."
  [config motor mode]
  {:pre [(or (= mode :on) (= mode :off))]}
  (write-attr config motor :speed-regulation (name mode)))

(defn read-regulation-mode
  "Reads the regulation mode of the motor."
  [config motor]
  (read-attr config motor :speed-regulation))

(defn set-polarity
  "Changes the forward direction of a motor.

  This is useful, for example, when you have two motors used as drive
  wheels. By changing the polarity of one of the two motors, you can
  send a positive value to both motors to drive forwards.

  Polarity can be set to :normal or :inversed. The default is :normal."
  [config motor polarity]
  {:pre [(some #{polarity} [:normal :inversed])]}
  (write-attr config motor :polarity polarity))

(defn read-polarity
  "Returns the current polarity setting."
  [config motor]
  (read-attr config motor :polarity))

(defn set-stop-behaviour
  "Sets a stop behavior of a motor. Allowed behaviours are:
  :brake, :coast and :hold.

  :coast - the power will be removed from the motor and it will coast to a stop
  :brake - passive braking; the motor controller removes power from the motor, but it
           also shorts the power wires of the motor together. When a motor is manually
           rotated, it acts as an electrical generator, so shorting the power wires
           creates a load that absorbs energy.
  :hold  - actively hold the motor position when stopping. Instead of removing power
           from the motor, the motor controller will start a PID to prevent the motor
           from being turned any further. It's intended for use with run-to-*-pos
           commands. It will work with other run commands, but may result in unexpected
           behaviour."
  [config motor behavior]
  {:pre [(some #{behavior} [:brake :coast :hold])]}

  (write-attr config motor :stop-command behavior))

;;;;;;;;;;;;; Run ;;;;;;;;;;;;;

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
      (set-speed config motor speed)
      (send-command config motor :run-forever))))

(defmethod run-motor :off [config motor speed _]
  (if (and (> speed 100) (< speed -100))
    (throw (Exception. "The speed must be in range [-100, 100]"))
    (do
      (write-duty-cycle config motor speed)
      (send-command config motor :run-forever))))

(defn run-forever
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
  ([config motor]
   (send-command config motor :run-forever))
  ([config motor speed]
   (let [speed-regulation (keyword (read-attr config motor :speed-regulation))]
     (run-motor config motor speed speed-regulation))))

(defn run-to-rel-pos
  "Runs the motor to relative position. Position is specified by :position-write
  but te value is added to the current position.

  This function can be invoked either without specifying position and using the
  current value, or with a position passed in, e.g.
  (run-to-rel-pos config motor)
  (run-to-rel-pos config motor 180)

  NOTE: Using a negative value for a position will cause the motor to rotate in
  the opposite direction."
  ([config motor]
   (send-command config motor :run-to-rel-pos))
  ([config motor position]
   (write-attr config motor :position-write position)
   (send-command config motor :run-to-rel-pos)))

(defn run-timed
  "Runs a motor for a specified time asynchronously.
  It starts the motor using :run-forever command and sets a timer in the kernel
  to run the :stop command after the specified time. The time is in milliseconds,
  and is written to :time-sp attribute."
  ([config motor]
   (send-command config motor :run-timed))
  ([config motor time]
   (write-attr config motor :time-sp time)
   (send-command config motor :run-timed)))

(defn run-direct
  "Runs a motor just like :run-forever, but changes to :duty-cycle take effect
  immediately instead of having to send a new command. To update the :duty-cycle
  run this command:
  (write-duty-cycle config motor 20)

  This is useful for implementing your own PID or something similar that needs to
  update the motor output very quickly."
  [config motor]
  (send-command config motor :run-direct))

(defn stop
  "Stops the motor:
  (stop config motor)

  The recommended way of stopping is to set the behaviout beforehand, e.g.
  (set-stop-behavior config motor :coast)
  There are three possible behaviours when the motor stops:
  :coast, :brake and :hold, described in more detail in set-stop-behavior
  docstring.

  We can also pass in required behavior to stop function:
  (stop config motor :coast)"
  ([config motor]
   (send-command config motor :stop))
  ([config motor behavior]
   (set-stop-behaviour config motor behavior)
   (send-command config motor :stop)))
