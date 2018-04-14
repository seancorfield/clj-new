(ns clj.new.template
  (:require [clj.new.templates :refer [project-name
                                       renderer sanitize year date ->files]]))

(defn template
  "A meta-template for 'clj new' templates."
  [name]
  (let [render (renderer "template")
        data {:raw-name name
              :name (project-name name)
              :sanitized (sanitize name)
              :placeholder "{{sanitized}}"
              :year (year)
              :date (date)}]
    (println "Generating a project called"
             (project-name name)
             "that is a 'clj-new' template")
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
