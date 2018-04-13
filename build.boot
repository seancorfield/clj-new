;; this build.boot file is temporary

(set-env! :resource-paths #{"src"})

(def version "0.1.0-SNAPSHOT")

(task-options!
 pom {:project     'seancorfield/clj-new
      :version     version
      :description "Generate projects from Boot templates."
      :url         "https://github.com/seancorfield/clj-new"
      :scm         {:url "https://github.com/seancorfield/clj-new"}
      :license     {"Eclipse Public License"
                    "http://www.eclipse.org/legal/epl-v10.html"}})

(deftask build []
  (comp (pom) (jar) (install)))

(deftask deploy
  []
  (comp (pom) (jar) (push)))
