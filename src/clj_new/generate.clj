;; copyright (c) 2018-2020 Sean Corfield, all rights reserved

(ns clj-new.generate
  "Command-line driver for generating new code into existing projects from
  templates, using just clj, and tools.deps."
  (:require [clj-new.helpers :as helpers]))

(defn -main
  "Bare bones entry point to create new code into an existing project.

  May eventually support more options."
  [& [generator & args]]
  (if generator
    (helpers/generate-code {:args args
                            :generate [generator]})
    (do
      (println "Usage: clj -m clj-new.generate generator\n")
      (println "Any additional arguments are passed directly to the generator.")
      (println "\nThe generator may be:")
      (println "* ns=the.ns - generate a new Clojure namespace")
      (println "* file=the.ns body - generate a new file for the namespace with the given body")
      (println "  - an optional argument can specify the extension (clj by default)")
      (println "* defn=the.ns/the-fn - generate a new defn for the-fn within the.ns")
      (println "* def=the.ns/the-sym - generate a new def for the-sym within the.ns")
      (println "  - an optional argument can specify the body (nil by default)")
      (println "* edn=the.ns body - generate a new edn file for the namespace with the given (optional) body")
      (println "  - an optional argument can specify the extension (edn by default)")))
  (shutdown-agents))
