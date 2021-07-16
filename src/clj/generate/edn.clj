(ns ^:no-doc clj.generate.edn
  (:require [clj.generate.file :as gen-file]))

(defn generate
  "Generate a new edn file relative to the prefix."
  [prefix ns-name & [body ext]]
  (gen-file/generate prefix ns-name (str body) (or ext "edn")))
