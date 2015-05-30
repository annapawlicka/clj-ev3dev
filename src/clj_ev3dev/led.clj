(ns clj-ev3dev.led
  (:require [clj-ev3dev.devices :as devices]))

(defn read-intensity
  "Reads the current brigtness of the led.
  Led passed has to be the result of
  running:
  clj-ev3dev.devices/find-led

  Returns a numeric value."
  [config sensor]
  (let [v (devices/read-attr config sensor :brightness)]
    (when-not (empty? v)
      (. Integer parseInt v))))

(defn max-intensity
  "Reads maximum brightness of the led.
  Led passed has to be the result of
  running:
  clj-ev3dev.devices/find-led

  Returns a numeric value."
  [config sensor]
  (let [v (devices/read-attr config sensor :max_brightness)]
    (when-not (empty? v)
      (. Integer parseInt v))))

(defn set-intensity
  "Sets the brightness of the led.
  Led passed has to be the result of
  running:
  clj-ev3dev.devices/find-led

  Intensity value should not exceed
  the maximum value for the led."
  [config sensor intensity]
  (devices/write-attr config sensor :brightness intensity))

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
  [config sensor]
  (let [trigger-str (devices/read-attr config sensor :trigger)]
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
  [config sensor mode]
  (if (contains? #{:none :mmc0 :timer :heartbeat :default-on :rfkill0} mode)
    (devices/write-attr config sensor :trigger (name mode))
    (throw (Exception. "Trigger must be one of the supported modes: :none, :mmc0, :timer, :heartbeat :default-on, :rfkill0."))))
