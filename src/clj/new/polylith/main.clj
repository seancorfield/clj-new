(ns {{namespace}}.cli.main
  (:require [{{namespace}}.greeter.interface :as greeter])
  (:gen-class))

(defn -main
  "Say Hello!"
  [& args]
  (println (greeter/greeting {:person (first args)})))
