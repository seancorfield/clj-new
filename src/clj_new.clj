;; copyright (c) 2020-2021 sean corfield, all rights reserved

(ns clj-new
  "Provides an API suitable for use with the Clojure CLI's `-X` option."
  (:require [clj-new.helpers :as h]))

(defn create
  "Public API for clojure -X usage."
  [options]
  (h/create-x options))

(defn app
  "Create new app project."
  [options]
  (h/create-x (assoc options :template "app")))

(defn lib
  "Create new lib project."
  [options]
  (h/create-x (assoc options :template "app")))

(defn template
  "Create new template project."
  [options]
  (h/create-x (assoc options :template "template")))

(defn generate
  "Public API for clojure -X usage."
  [options]
  (h/generate-x options))
