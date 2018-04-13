;; copyright (c) 2018 Sean Corfield, all rights reserved

(ns clj-new.create
  "Command-line driver for creating new projects from templates, using just
  clj, tools.deps, and pomegranate (for the dynamic classpath).

  Supports clj-template, boot-template, and lein-template projects."
  (:require [clj-new.helpers :as helpers]))

(defn -main
  "Bare bones entry point to create a new project from a template.

  May eventually support more options."
  [template-name project-name & args]
  (if (and template-name project-name)
    (helpers/create {:template template-name
                     :name project-name
                     :args args
                     :verbose nil})
    (do
      (println "Usage: clj -m clj-new.create template-name project-name\n")
      (println "Any additional arguments are passed directly to the template.")
      (println "\nThe template-name may be:")
      (println "* app - create a new application based on deps.edn")
      (println "* lib - create a new library based on deps.edn")
      (println "* template - create a new clj template based on deps.edn")
      (println "\nThe project-name must be a valid Clojure symbol.")))
  (shutdown-agents))
