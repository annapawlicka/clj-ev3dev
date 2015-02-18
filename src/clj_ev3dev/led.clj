(ns clj-ev3dev.led
  (:require [clj-ev3dev.core :as core]))

(defn read-intensity
  "Reads the current brigtness of the led.
  Led passed has to be the result of
  running:
  clj-ev3dev.devices/find-led

  Returns a numeric value."
  [session led]
  (let [cmd (str "cat /sys/class/leds/" led "/brightness")]
    (. Integer parseInt (core/execute session cmd))))

(defn max-intensity
  "Reads maximum brightness of the led.
  Led passed has to be the result of
  running:
  clj-ev3dev.devices/find-led

  Returns a numeric value."
  [session led]
  (let [cmd (str "cat /sys/class/leds/" led "/max_brightness")]
    (. Integer parseInt (core/execute session cmd))))

(defn set-intensity
  "Sets the brightness of the led.
  Led passed has to be the result of
  running:
  clj-ev3dev.devices/find-led

  Intensity value should not exceed
  the maximum value for the led."
  [session led intensity]
  (let [cmd (str "echo " intensity " > /sys/class/leds/" led "/brightness")]
    (core/execute session cmd)))

(defn find-mode
  "Finds selected mode, strips it off square
  brackets and keywordises it."
  [trigger-str]
  (-> (re-find #"\[\S+\]" trigger-str)
      (clojure.string/replace #"\[" "")
      (clojure.string/replace #"\]" "")
      keyword))

(defn read-trigger
  "Returns a keyword representation of the set trigger."
  [session led]
  (let [cmd         (str "cat /sys/class/leds/" led "/trigger")
        trigger-str (core/execute session cmd)]
    (find-mode trigger-str)))

(defn set-trigger
  "Triggers can make the LED do interesting things.

  Available modes:

  :none       - manually control the LED with brightness
  :mmc0       - makes the LED blink whenever there is SD card activity.
  :timer      - makes the LED blink. When we change the trigger,
                we get new attributes for controlling the on and
                off times. Times are in milliseconds.
  :heartbeat  - makes the LED blink at a rate proportional to CPU usage.
  :default-on - works just like none except that it turns the LED on
                when the trigger is set.
  :rfkill0    - the RF (radio frequency) kill switch for the built-in
                Bluetooth. It should make the LED indicate if the built-in
                Bluetooth is turned on or not.

  All legoev3-battery-*  options are omitted as they are not useful.
  The batteries (including the rechargeable battery pack) do not have
  a way of telling the EV3 about their state, so it is assumed that the
  batteries are always discharging. Therefore these triggers will
  always turn the LED off."
  [session led mode]
  (if (contains? #{:none :mmc0 :timer :heartbeat :default-on :rfkill0} mode)
    (let [cmd (str "echo \"" (name mode) "\" > "
                   "/sys/class/leds/" led "/trigger")]
      (core/execute session cmd))
    (throw (Exception. "Trigger must be one of the supported modes: :none, :mmc0, :timer, :heartbeat :default-on, :rfkill0."))))
