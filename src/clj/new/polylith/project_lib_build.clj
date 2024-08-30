(ns build
  (:refer-clojure :exclude [test])
  (:require [clojure.tools.deps :as t]
            [clojure.tools.build.api :as b]
            [deps-deploy.deps-deploy :as dd]))

(def lib '{{group}}/{{artifact}})
(def version "{{version}}")
#_ ; alternatively, use MAJOR.MINOR.COMMITS:
(def version (format "1.0.%s" (b/git-count-revs nil)))
(def class-dir "target/classes")

(defn- lifted-basis
  "This creates a basis where source deps have their primary
  external dependencies lifted to the top-level, such as is
  needed by Polylith and possibly other monorepo setups."
  []
  (let [default-libs  (:libs (b/create-basis))
        source-dep?   #(not (:mvn/version (get default-libs %)))
        lifted-deps
        (reduce-kv (fn [deps lib {:keys [dependents] :as coords}]
                     (if (and (contains? coords :mvn/version) (some source-dep? dependents))
                       (assoc deps lib (select-keys coords [:mvn/version :exclusions]))
                       deps))
                   {}
                   default-libs)]
    (-> (b/create-basis {:extra {:deps lifted-deps}})
        (update :libs #(into {} (filter (comp :mvn/version val)) %)))))

(defn- pom-template [version]
  [[:description "{{description}}"]
   [:url "https://{{scm-domain}}/{{scm-user}}/{{artifact}}"]
   [:licenses
    [:license
     [:name "Eclipse Public License"]
     [:url "http://www.eclipse.org/legal/epl-v10.html"]]]
   [:developers
    [:developer
     [:name "{{developer}}"]]]
   [:scm
    [:url "https://{{scm-domain}}/{{scm-user}}/{{artifact}}"]
    [:connection "scm:git:https://{{scm-domain}}/{{scm-user}}/{{artifact}}.git"]
    [:developerConnection "scm:git:ssh:git@{{scm-domain}}:{{scm-user}}/{{artifact}}.git"]
    [:tag (str "v" version)]]])

(defn- jar-opts [opts])

(defn- jar-opts [opts]
  (let [basis      (lifted-basis)
        directory? #(let [f (java.io.File. %)]
                      (and (.exists f) (.isDirectory f)))
        src+dirs   (filter directory? (:classpath-roots basis))]
    (assoc opts
           :lib lib   :version version
           :jar-file  (format "target/%s-%s.jar" lib version)
           :basis     lifted-basis
           :class-dir class-dir
           :target    "target"
           :src-dirs  src+dirs
           :pom-data  (pom-template version))))

(defn jar "Build the JAR." [opts]
  (b/delete {:path "target"})
  (let [opts (jar-opts opts)]
    (println "\nWriting pom.xml...")
    (b/write-pom opts)
    (println "\nCopying source...")
    (b/copy-dir {:src-dirs (into ["resources" "src"] (:src-dirs opts)) :target-dir class-dir})
    (println "\nBuilding JAR...")
    (b/jar opts))
  opts)

(defn install "Install the JAR locally." [opts]
  (let [opts (jar-opts opts)]
    (b/install opts))
  opts)

(defn deploy "Deploy the JAR to Clojars." [opts]
  (let [{:keys [jar-file] :as opts} (jar-opts opts)]
    (dd/deploy {:installer :remote :artifact (b/resolve-path jar-file)
                :pom-file (b/pom-path (select-keys opts [:lib :class-dir]))}))
  opts)
