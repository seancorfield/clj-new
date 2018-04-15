(ns clj.generate.edn
  (:require [clj.generate.file :as gen-file]
            [clj.new.templates :as tmpl]))

(defn generate
  "Generate a new edn file relative to the prefix."
  [prefix ns-name & [body ext]]
  (gen-file/generate prefix ns-name (str body) (or ext "edn")))
