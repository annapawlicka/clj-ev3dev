(defproject clj-ev3dev-exampleapp "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.7.0-RC1"]
                 [clj-ev3dev "0.1.0-SNAPSHOT"]]
  :aot [clj-ev3dev-exampleapp.core]
  :main clj-ev3dev-exampleapp.core)
