(defproject clj-ev3dev "0.1.0-SNAPSHOT"
  :description "Clojure wrapper around ev3dev API."
  :url "https://github.com/annapawlicka/clj-ev3dev"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :signing {:gpg-key "pawlicka.anna@gmail.com"}
  :deploy-repositories [["clojars" {:creds :gpg}]]

  :jar-name "clj-ev3dev-%s.jar"

  :dependencies [[org.clojure/clojure "1.7.0-RC1"]
                 [clj-ssh "0.5.11"]]

  :plugins [[codox "0.8.10"]])
