(ns ^:no-doc clj.new.polylith
  "Generate a basic Polylith monorepo project."
  (:require [clj.new.templates
             :refer [renderer project-data project-dir project-name ->files]]
            [clojure.java.shell :as sh]))

(defn polylith
  "A Polylith monorepo project template."
  [name & _]
  (let [render (renderer "polylith")
        data   (merge {:description "FIXME: my new application."}
                      (project-data name))]
    (println "Generating a project called"
             (project-name name)
             "based on the 'polylith' template.")
    (->files data
             ["bases/cli/deps.edn" (render "base_deps.edn" data)]
             ["bases/cli/src/{{nested-dirs}}/cli/main.clj" (render "main.clj" data)]
             ["bases/cli/resources/.keep" ""]
             ["bases/cli/test/{{nested-dirs}}/cli/main_test.clj" (render "main_test.clj" data)]
             ["components/greeter/deps.edn" (render "component_deps.edn" data)]
             ["components/greeter/src/{{nested-dirs}}/greeter/interface.clj" (render "interface.clj" data)]
             ["components/greeter/src/{{nested-dirs}}/greeter/core.clj" (render "core.clj" data)]
             ["components/greeter/resources/.keep" ""]
             ["components/greeter/test/{{nested-dirs}}/greeter/interface_test.clj" (render "interface_test.clj" data)]
             ["deps.edn" (render "root_deps.edn" data)]
             ["development/src/dev.clj" (render "dev-clj" data)]
             ["projects/{{name}}/deps.edn" (render "project_app_deps.edn" data)]
             ["projects/{{name}}/test/{{nested-dirs}}/{{name}}_test.clj" (render "project_test.clj" data)]
             ["projects/{{name}}-lib/deps.edn" (render "project_lib_deps.edn" data)]
             ["projects/{{name}}-lib/pom.xml" (render "pom.xml" data)]
             ["README.md" (render "README.md" data)]
             ["workspace.edn" (render "workspace.edn" data)]
             [".gitignore" (render "gitignore" data)]
             ["LICENSE" (render "LICENSE" data)]
             ["CHANGELOG.md" (render "CHANGELOG.md" data)])
    ;; now do the git setup for the project
    (let [dir (project-dir (:name data))
          {:keys [exit out err]} (sh/sh "git" "init" :dir dir)]
      (if (zero? exit)
        (let [{:keys [exit out err]} (sh/sh "git" "add" "." :dir dir)]
          (if (zero? exit)
            (let [{:keys [exit out err]} (sh/sh "git" "commit" "-m" "\"Workspace created (by clj-new).\"" :dir dir)]
              (if (zero? exit)
                (println "Initialized the project for use with 'git'.")
                (do
                  (println "Unable to commit the new workspace to 'git'.")
                  (when (seq out) (println out))
                  (when (seq err) (println err)))))
            (do
              (println "Unable to add the new workspace to 'git'.")
              (when (seq out) (println out))
              (when (seq err) (println err)))))
        (do
          (println "Unable to initialize the new workspace for 'git':")
          (when (seq out) (println out))
          (when (seq err) (println err)))))))
