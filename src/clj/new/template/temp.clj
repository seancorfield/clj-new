(ns clj.new.{{name}}
  (:require [clj.new.templates :refer [renderer name-to-path ->files]]))

(def render (renderer "{{name}}"))

(defn {{name}}
  "FIXME: write documentation"
  [name]
  (let [data {:name name
              :sanitized (name-to-path name)}]
    (println "Generating fresh 'clj new' {{name}} project.")
    (->files data
             ["src/{{placeholder}}/foo.clj" (render "foo.clj" data)])))
