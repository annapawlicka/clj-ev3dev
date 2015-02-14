# clj-ev3dev

WIP: A Clojure wrapper around ev3dev API.

## Getting Started

### ev3dev

We need to install ev3dev onto a Micro SD card (by using an SD card we
can keep EV3's built-in software intact). Instructions for the
installation process can be found on ev3dev's
[Getting Started page](http://www.ev3dev.org/docs/getting-started/). The
following assumes that you have chosen the latest ev3dev's
release. When you're done, reboot your EV3 and make sure you can ssh
into it from your computer.

The ev3dev distribution is a full Debian (jessie) Linux distribution,
and it includes built-in ssh support and custom drivers for EV3's
hardware.
We can very easily interact with EV3's sensors, motors, etc. via a
simple file system-based interface. Directories under `/sys/class`
represent various device classes, and setting attributes is as simple
as writing to files.


### Clojure

My initial idea was to have a Clojure application running on EV3 and use
`clojure.java.io` or simply `slurp/spit` files. But considering that
the CPU is ARM9 300MHz and RAM is 16 MB Flash &
64 MB RAM, it's just too slow. I've tried using OpenJDK Zero VM
and `lein trampoline repl`, to no avail.

I've settled on sending commands over ssh. This library
wraps shell commands and sends them over ssh. And although you
can send those commands yourself, doing `cat
/sys/class/msensor/sensor0/value0` is tedious, especially that each time you boot
EV3 the devices (sensors & motors) are mapped to different nodes that do not
correspond to the in ports. It’s much easier when
you have a higher-level library to use that handles that for you.

By running your application on your machine you benefit from a gazillion
times faster CPU that can crunch those algorithms in no time. And you
save EV3's battery too.

### JSchException: Algorithm negotiation fail

There are a few places that SSH clients and servers try and
agree on a common implementation, one of them is encryption. Ev3dev
distro comes with openSSH that has several kex algorithms disabled by
default. You'll need to add them to your
`/etc/ssh/sshd_config` on EV3. These are these additional algos:
`diffie-hellman-group1-sha1` and
`diffie-hellman-group-exchange-sha1`.

Add this to the bottom of `/etc/ssh/sshd_config` : (adding
KexAlgorithms overrides the config so you should add all default algos)

```
KexAlgorithms curve25519-sha256 at libssh.org,diffie-hellman-group1-sha1,diffie-hellman-group14-sha1,diffie-hellman-group-exchange-sha1,diffie-hellman-group-exchange-sha256,ecdh-sha2-nistp256,ecdh-sha2-nistp384,ecdh-sha2-nistp521
```
Restart openSSH server on your server and try connecting again:

```shell
/etc/init.d/ssh restart
```

## Usage

To read/write sensor/motor state:

```clojure
user=> (use 'clj-ev3dev.core)
user=> (def session (create-session {:ip "192.168.2.3" :username
"username" :password "password" :strict-host-key-checking :no}))

user=> (use 'clj-ev3dev.sensors.infrared)
user=> (def infrared-sensor (find-infrared-sensor session)
user=> (read-proximity session sensor)
       44

user=> (use 'clj-ev3dev.sensors.touch)
user=> (def touch-sensor (find-touch-sensor session))
user=> (pressed? session touch-sensor)
       true

user=> (use 'clj-ev3dev.sensors.color)
user=> (def color-sensor (find-color-sensor session))
user=> (read-color session color-sensor)
       :red
user=> (read-reflected-light-intensity session color-sensor)
       23

user=> (use 'clj-ev3dev.devices)
user=> (def red-left (find-led :red-left)) ;; :red-right, :green-left, :green-right
user=> (use 'clj-ev3dev.motors.led)
user=> (read-intensity session red-left)
       0
user=> (max-intensity session red-left)
       255
user=> (set-intensity session red-left 75)

```

## License

Copyright © 2015 Anna Pawlicka

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
