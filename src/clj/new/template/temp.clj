(ns clj.new.{{name}}
  (:require [clj.new.templates :refer [renderer project-name name-to-path ->files
                                       multi-segment sanitize-ns]]))

(def render (renderer "{{name}}"))

(defn {{name}}
  "FIXME: write documentation"
  [name]
  (let [data {:name (project-name name)
              :sanitized (name-to-path name)
              :root-ns (multi-segment (sanitize-ns name))}]
    (println "Generating fresh 'clj new' {{name}} project.")
    (->files data
             ["deps.edn" (render "deps.edn" data)]
             ["src/{{placeholder}}/foo.clj" (render "foo.clj" data)])))
