;; copyright (c) 2018-2021 Sean Corfield, all rights reserved

(ns ^:no-doc clj-new.create
  "Command-line driver for creating new projects from templates, using just
  clj, tools.deps, and pomegranate (for the dynamic classpath).

  Supports clj-template, boot-template, and lein-template projects."
  (:require [clj-new.helpers :as helpers]))

(defn -main
  "Bare bones entry point to create a new project from a template.

  May eventually support more options."
  [& [template-name project-name & args]]
  (helpers/create {:template template-name
                   :name project-name
                   :args args}))
