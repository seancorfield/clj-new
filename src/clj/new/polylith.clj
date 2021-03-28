(ns clj.new.polylith
  "Generate a basic Polylith monorepo project."
  (:require [clj.new.templates
             :refer [renderer project-data project-name ->files]]))

(defn polylith
  "A Polylith monorepo project template."
  [name & args]
  (let [render (renderer "polylith")
        data   (merge {:description "FIXME: my new application."}
                      (project-data name))]
    (println "Generating a project called"
             (project-name name)
             "based on the 'polylith' template.")
    (comment
      :structure
      . :bases
      . . :cli
      . . . :src
      . . . . :api.clj
      . . :deps.edn - :b_deps.edn
      . :components
      . . :greeter
      . . . :src
      . . . . :interface.clj -
      . . . . :core.clj -
      . :deps.edn - :root_deps.edn
      . :development
      . . :src
      . . . :dev.clj
      . :projects
      . . :cli
      . . . :deps.edn - :project_deps.edn
      . :README.md
      . :workspace.edn
      .)
    (->files data
             ["bases/cli/deps.edn" (render "b_deps.edn" data)]
             ["bases/cli/src/{{nested-dirs}}/cli/main.clj" (render "main.clj" data)]
             ["bases/cli/resources/.keep" ""]
             ["bases/cli/test/{{nested-dirs}}/cli/main_test.clj" (render "main_test.clj" data)]
             ["components/greeter/deps.edn" (render "c_deps.edn" data)]
             ["components/greeter/src/{{nested-dirs}}/greeter/interface.clj" (render "interface.clj" data)]
             ["components/greeter/src/{{nested-dirs}}/greeter/core.clj" (render "core.clj" data)]
             ["components/greeter/resources/.keep" ""]
             ["components/greeter/test/{{nested-dirs}}/greeter/interface_test.clj" (render "interface_test.clj" data)]
             ["deps.edn" (render "root_deps.edn" data)]
             ["development/src/dev.clj" (render "dev.clj" data)]
             ["projects/{{name}}/deps.edn" (render "project_deps.edn" data)]
             ["projects/{{name}}/pom.xml" (render "pom.xml" data)]
             ["projects/{{name}}/test/{{nested-dirs}}/{{name}}_test.clj" (render "project_test.clj" data)]
             ["README.md" (render "README.md" data)]
             ["workspace.edn" (render "workspace.edn" data)]
             [".gitignore" (render "gitignore" data)]
             ["LICENSE" (render "LICENSE" data)]
             ["CHANGELOG.md" (render "CHANGELOG.md" data)])))
