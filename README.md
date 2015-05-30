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

### Running on the robot itself (local)

Add `clj-ev3dev` to your project's dependencies:

```clojure
[clj-ev3dev "0.1.0-SNAPSHOT"]
```

You need create a configuration map that will specify the environment
that you want to use and will be passed to all functions:

```clojure
user=> (def config {:env :local})
```

To find node names of connected sensors and motors,
to read and write mode:

```clojure
user=> (require '[clj-ev3dev.devices :as devices])
```

Infrared sensor:

```clojure

user=> (require '[clj-ev3dev.sensors.infrared :as infrared])
user=> (def infrared-sensor (devices/find-sensor config {:device-type
:infrared :port :4}))
user=> (infrared/read-proximity config infrared-sensor)
       44

```

Touch sensor:

```clojure

user=> (require '[clj-ev3dev.sensors.touch :as touch])
user=> (def touch-sensor (devices/find-sensor config {:device-type
:touch :port :1}))
user=> (touch/pressed? config touch-sensor)
       true

```

Color sensor:


```clojure

user=> (require '[clj-ev3dev.sensors.color :as color])
user=> (def color-sensor (devices/find-sensor config {:device-type
:color :port :3}))
user=> (devices/read-mode config color-sensor)
user=> :col-color
user=> (color/read-color config color-sensor)
       :red
user=> (devices/write-mode config color-sensor :col-reflect)
user=> (color/read-reflected-light-intensity config color-sensor)
       23
```

LEDs:

```clojure

user=> (def red-left (devices/find-led :red-left)) ;; :red-right, :green-left, :green-right
user=> (require '[clj-ev3dev.led :as led])
user=> (led/read-intensity config red-left)
       0
user=> (led/max-intensity config red-left)
       255
user=> (led/set-intensity config red-left 75)

```

Tacho motors:

```clojure

user=> (require '[clj-ev3dev.motors.tacho :as tacho])
user=> (def motor-left (tacho/find-tacho-motor config :b))
;; runs the left motor with very  slow speed
user=> (tacho/run config motor-left 20)
user=> (tacho/stop config motor-left)   ;; stops the motor
```

#### Example application

Please see `demos` directory for examples of applications.

### Comunicating over ssh (remote)

This option allows you to control EV3 remotely, which means you can
run your program on your machine, try things out, and once you're
happy with the results you can build uberjar replacing `:env :remote`
with `:env :local` in your config map and run in on the brick. A word of warning: memory hungry operations will run ok on you machine but may not run at all on ev3. Keep its specs in mind. You can also just stick to running it locally. Whatever works for you :)

Remote mode wraps shell commands and sends them over ssh. And although you can send those commands yourself, doing `cat /sys/class/lego-sensor/sensor0/value0` is tedious, especially that each time you boot EV3 the devices (sensors & motors) are mapped to different nodes that do not correspond to the in ports. It’s much easier when you have a higher-level library to use that handles that for you.

By running your application on your machine you benefit from a
gazillion times faster CPU that can crunch those algorithms in no
time. And you save EV3's battery too.

You must add session to your config map. To establish session:

```clojure

user=> (use 'clj-ev3dev.ssh)
user=> (def session (create-session {:ip-address "192.168.2.3" :username
"username" :password "password" :strict-host-key-checking :no}))
user=> (def config {:env :remote :session session})

```

To find node names of connected sensors and motors,
to read and write mode:

```clojure
user=> (require '[clj-ev3dev.devices :as devices])
```

Infrared sensor:

```clojure

user=> (require '[clj-ev3dev.sensors.infrared :as infrared])
user=> (def infrared-sensor (devices/find-sensor config {:device-type
:infrared :port :four}))
user=> (infrared/read-proximity config infrared-sensor)
       44

```

Touch sensor:

```clojure

user=> (require '[clj-ev3dev.sensors.touch :as touch])
user=> (def touch-sensor (devices/find-sensor config {:device-type
:touch :port :1}))
user=> (touch/pressed? config touch-sensor)
       true

```

Color sensor:


```clojure

user=> (require '[clj-ev3dev.sensors.color :as color])
user=> (def color-sensor (devices/find-sensor config {:device-type
:color :port :one}))
user=> (devices/read-mode config color-sensor)
user=> :col-color
user=> (color/read-color config color-sensor)
       :red
user=> (devices/write-mode config color-sensor :col-reflect)
user=> (color/read-reflected-light-intensity config color-sensor)
       23
```

LEDs:

```clojure

user=> (def red-left (devices/find-led :red-left)) ;; :red-right, :green-left, :green-right
user=> (require '[clj-ev3dev.led :as led])
user=> (led/read-intensity config red-left)
       0
user=> (led/max-intensity config red-left)
       255
user=> (led/set-intensity config red-left 75)

```

Tacho motors:

```clojure

user=> (require '[clj-ev3dev.motors.tacho :as tacho])
user=> (def motor-left (tacho/find-tacho-motor config :b))
;; runs the left motor with very  slow speed
user=> (tacho/run config motor-left 20)
user=> (tacho/stop config motor-left)   ;; stops the motor
```


## License

Copyright © 2015 Anna Pawlicka

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
