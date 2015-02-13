(ns clj-ev3dev.core
  (:require [clj-ssh.ssh :as ssh]))

(defn create-session
  "Creates ssh session using provided config.

  Config example:
  {:ip-address \"192.168.2.3\" :username \"username\"
   :password \"password\" :strict-host-key-checking :no} "
  [config]
  (let [agent (ssh/ssh-agent {})]
    (ssh/session agent (:ip-address config) config)))

(defn connect
  "Connects the session. If the session has disconneted,
  it will automatically reconnect."
  [session]
  (if-not (ssh/connected? session)
    (ssh/connect session)
    session))

(defn execute
  "Sends a command to the brick using existing ssh session.
  It doesn't disconnect the session on exit to reduce time
  it takes to reconnect. Commands are send too frequently
  to create a new session each time."
  [session command]
  (connect session)
  (let [result (ssh/ssh session {:cmd command})]
    (clojure.string/trim-newline (result :out))))
