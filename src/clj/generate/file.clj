(ns clj.generate.file
  (:require [clj.new.templates :as tmpl]))

(defn generate
  "Generate a new file relative to the prefix."
  [prefix fs-name & [body ext append?]]
  (tmpl/->files {:prefix prefix :path (tmpl/name-to-path fs-name) :ext (or ext "clj")}
                ["{{prefix}}/{{path}}.{{ext}}" (str body "\n") :append (or append? false)]))
