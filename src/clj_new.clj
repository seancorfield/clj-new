;; copyright (c) 2020 sean corfield, all rights reserved

(ns clj-new
  "Provides an API suitable for use with the Clojure CLI's `-X` option."
  (:require [clj-new.helpers :as h]))

(defn create
  "Public API for clojure -X usage."
  [options]
  (h/create-x options))

(defn generate
  "Public API for clojure -X usage."
  [options]
  (h/generate-x options))
