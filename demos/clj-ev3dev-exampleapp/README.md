# clj-ev3dev-exampleapp

A simple application that tests the presence of sensors and motors.

## Usage


Create an uberjar:
```shell
lein compile
lein uberjar
```
and copy it to the robot:
```shell
scp target/clj-ev3dev-exampleapp-0.1.0-SNAPSHOT-standalone.jar ev3dev:
```
Ssh onto the robot and run the application:
```shell
java -jar clj-ev3dev-exampleapp-0.1.0-SNAPSHOT-standalone.jar
```

## License

Copyright © 2015 Anna Pawlicka

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
