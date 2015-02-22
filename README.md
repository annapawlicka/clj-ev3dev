# clj-ev3dev

WIP: A Clojure wrapper around ev3dev API.

Once the library is more stable it will drop `-SNAPSHOT`.

```clojure
[clj-ev3dev "0.1.0-SNAPSHOT"]
```

## Getting Started

### ev3dev

We need to install ev3dev onto a Micro SD card (by using an SD card we
can keep EV3's built-in software intact). Instructions for the
installation process can be found on ev3dev's
[Getting Started page](http://www.ev3dev.org/docs/getting-started/). The
following assumes that you have chosen the latest ev3dev's
release. When you're done, reboot your EV3 and make sure you can ssh
into it from your computer. Working connection is required to be able
to copy your Clojure controller to the brick and run it.


### Clojure

First install java on EV3.
To run your Clojure robot controller, you need to create an uberjar
and copy it to ev3. Then you can run it using the following command:

```
java -jar name-of-your-standalone.jar
```

But deploying the jar each time you want to test something is a little
cumbersome. For this reason I've created
[clj-ev3dev-remote](https://github.com/annapawlicka/clj-ev3dev-remote)

If you'd like to play with ev3 first, or you're ok with running it
remotely, `clj-ev3dev-remote` will execute all your commands through
ssh.

### Performance
While you're writing your controller, you have to keep in mind
ev3's specs. The startup time takes a few minutes, and if you're
going to do any memory heavy operations you're very
likely going to see `java.lang.OutOfMemoryError: Java heap space`.

## Usage

Add `clj-ev3dev` to your project's dependencies:

```clojure
[clj-ev3dev "0.1.0-SNAPSHOT"]
```

To find node names of connected sensors and motors,
to read and write mode:

```clojure
user=> (require '[clj-ev3dev.devices :as devices])
```

Infrared sensor:

```clojure

user=> (require '[clj-ev3dev.sensors.infrared :as infrared])
user=> (def infrared-sensor (devices/find-sensor :infrared :4))
user=> (infrared/read-proximity infrared-sensor)
       44

```

Touch sensor:

```clojure

user=> (require '[clj-ev3dev.sensors.touch :as touch])
user=> (def touch-sensor (devices/find-sensor :touch :1))
user=> (touch/pressed? touch-sensor)
       true

```

Color sensor:


```clojure

user=> (require '[clj-ev3dev.sensors.color :as color])
user=> (def color-sensor (devices/find-sensor :color :3))
user=> (devices/read-mode color-sensor)
user=> :col-color
user=> (color/read-color color-sensor)
       :red
user=> (devices/write-mode color-sensor :col-reflect)
user=> (color/read-reflected-light-intensity color-sensor)
       23
```

LEDs:

```clojure

user=> (def red-left (devices/find-led :red-left)) ;; :red-right, :green-left, :green-right
user=> (require '[clj-ev3dev.led :as led])
user=> (led/read-intensity red-left)
       0
user=> (led/max-intensity red-left)
       255
user=> (led/set-intensity red-left 75)

```

Tacho motors:

```clojure

user=> (require '[clj-ev3dev.motors.tacho :as tacho])
user=> (def motor-left (tacho/find-tacho-motor :b))
;; runs the left motor with very  slow speed
user=> (tacho/run motor-left 20)
user=> (tacho/stop motor-left)   ;; stops the motor
```

### Example application

```clojure
(ns sample-controller.core
  (:require [clj-ev3dev.led              :as led]
            [clj-ev3dev.devices          :as devices]
            [clj-ev3dev.motors.tacho     :as tacho]
            [clj-ev3dev.sensors.color    :as color]
            [clj-ev3dev.sensors.infrared :as infrared]
            [clj-ev3dev.sensors.touch    :as touch])
  (:gen-class :main true))

(defn test-leds []
  (let [left-red    (devices/find-led :red-left)
        left-green  (devices/find-led :green-left)
        right-red   (devices/find-led :red-right)
        right-green (devices/find-led :green-right)]
    (println "LEDS")
    (led/set-trigger left-red :heartbeat)
    (led/set-trigger left-red :none)
    (led/set-trigger left-green :default-on)))

(defn test-motors []
  (println "MOTORS")
  (let [left-motor  (tacho/find-tacho-motor :b)
        right-motor (tacho/find-tacho-motor :c)]
    (tacho/run left-motor 20)
    (tacho/run right-motor 20)
    (tacho/stop left-motor)
    (tacho/stop right-motor)))

(defn test-color-sensor []
  (println "COLOR")
  (let [color-sensor (devices/find-sensor :color :2)]
    (devices/write-mode color-sensor :col-color)
    (println "Color: " (color/read-color color-sensor))))

(defn test-infrared-sensor []
  (println "INFRARED")
  (let [infrared-sensor (devices/find-sensor :infrared :4)]
    (println "Proximity: " (infrared/read-proximity infrared-sensor))))

(defn test-touch-sensor []
  (println "TOUCH")
  (let [touch-sensor (devices/find-sensor :touch :1)]
    (while (not (touch/pressed? touch-sensor))
      (println "Waiting for you to press me o.0")
      (Thread/sleep 1000))))

(defn -main
  "The application's main function"
  [& args]
  (test-leds)
  (test-motors)
  (test-color-sensor)
  (test-infrared-sensor)
  (test-touch-sensor))
```

## License

Copyright © 2015 Anna Pawlicka

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
