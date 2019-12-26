(ns clj.new.{{name}}
  (:require [clj.new.templates :refer [renderer project-data ->files]]))

(defn {{name}}
  "FIXME: write documentation"
  [name]
  (let [render (renderer "{{name}}")
        data   (project-data name)]
    (println "Generating fresh 'clj new' {{name}} project.")
    (->files data
             ["deps.edn" (render "deps.edn" data)]
             ["src/{{template-nested-dirs}}/foo.clj" (render "foo.clj" data)])))
