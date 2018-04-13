(ns clj.new.template
  (:require [clj.new.templates :refer [renderer sanitize year date ->files]]))

(defn template
  "A meta-template for 'clj new' templates."
  [name]
  (let [render (renderer "template")
        data {:name name
              :sanitized (sanitize name)
              :placeholder "{{sanitized}}"
              :year (year)
              :date (date)}]
    (println "Generating fresh 'clj new' template project.")
    (->files data
             ["deps.edn" (render "deps.edn" data)]
             ["README.md" (render "README.md" data)]
             [".gitignore" (render "gitignore" data)]
             [".hgignore" (render "hgignore" data)]
             ["src/clj/new/{{sanitized}}.clj" (render "temp.clj" data)]
             ["resources/clj/new/{{sanitized}}/foo.clj" (render "foo.clj")]
             ["resources/clj/new/{{sanitized}}/deps.edn" (render "deps.edn" data)]
             ["LICENSE" (render "LICENSE" data)]
             ["CHANGELOG.md" (render "CHANGELOG.md" data)])))
