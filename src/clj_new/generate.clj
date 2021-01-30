;; copyright (c) 2018-2021 Sean Corfield, all rights reserved

(ns clj-new.generate
  "Command-line driver for generating new code into existing projects from
  templates, using just clj, and tools.deps."
  (:require [clj-new.helpers :as helpers]))

(defn -main
  "Bare bones entry point to create new code into an existing project.

  May eventually support more options."
  [& [generator & args]]
  (helpers/generate-code {:args args
                          :generate [generator]}))
