(ns clj-new.helpers
  "The top-level logic for the clj-new create/generate entry points."
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [clojure.stacktrace :as stack]
            [clojure.string :as str]
            [clojure.tools.cli :as cli]
            [clojure.tools.deps.alpha :as deps]
            [clojure.tools.deps.alpha.util.session :as session]
            ;; support boot-template projects:
            [boot.new.templates :as bnt]
            ;; needed for dynamic classloader/add-classpath stuff:
            [cemerick.pomegranate :as pom]
            ;; support clj-template projects:
            [clj.new.templates :as cnt]
            ;; support lein-template projects:
            [leiningen.new.templates :as lnt])
  (:import java.io.FileNotFoundException))

(def ^:dynamic *debug* nil)
(def ^:dynamic *use-snapshots?* false)
(def ^:dynamic *template-version* nil)

(defn resolve-and-load
  "Given a deps map and an extra-deps map, resolve the dependencies, figure
  out the classpath, and load everything into our (now dynamic) classloader."
  [deps resolve-args]
  (session/with-session
    (-> (deps/resolve-deps deps resolve-args)
        (deps/make-classpath (:paths deps) {})
        (str/split (re-pattern java.io.File/pathSeparator))
        (->> (run! pom/add-classpath)))))

(def ^:private git-url-sha #"(https?://[^/]+/[^/]+/([^/@]+)).*@([a-fA-Z0-9]+)")
(def ^:private git-path-template #"(https?://[^/]+/[^/]+/[^/]+)/((.*/)?([^/]+))@([a-fA-Z0-9]+)")

(comment
  (re-find git-url-sha "https://github.com/org-name/repo-name@abc")
  (re-find git-url-sha "https://github.com/org-name/repo-a/to/template-name@abc")
  (re-find git-path-template "https://github.com/org-name/repo-name@abc")
  (re-find git-path-template "https://github.com/org-name/repo-a/to/template-name@abc"))

(def ^:private local-root  #"(.+)::(.+)")

(defn- more-debugging
  "Return a string that can be added to exception messages to suggest
  using more verbose logging in order to debug problems."
  []
  (if *debug*
    (if (< *debug* 3)
      (str "\n\nFor more detail, increase verbose logging with "
           (case *debug*
             0 "-v, -vv, or -vvv"
             1 "-vv or -vvv"
             2 "-vvv"))
      "")
    "\n\nFor more detail, enable verbose logging with -v, -vv, or -vvv"))

(def ^:private basis
  "Return the runtime basis from the Clojure CLI invocation.

  If we're running under an older CLI version, construct the
  equivalent of the basis manually."
  (delay (if-let [basis-file (System/getProperty "clojure.basis")]
           (-> basis-file
               (io/file)
               (slurp)
               (edn/read-string))
           (let [{:keys [root-edn user-edn project-edn]} (deps/find-edn-maps)]
             (deps/merge-edns (filterv some? [root-edn user-edn project-edn]))))))

(defn resolve-remote-template
  "Given a template name, attempt to resolve it as a clj template first, then
  as a Boot template, then as a Leiningen template. Return the type of template
  we found and the final, derived template-name."
  [template-name]
  (let [selected      (atom nil)
        failure       (atom nil)
        tmp-version   (cond *template-version* *template-version*
                            *use-snapshots?*   "(0.0.0,)"
                            :else              "RELEASE")
        [_ git-url git-tmp-name-1 sha]    (re-find git-url-sha template-name)
        [_ _ git-path _ git-tmp-name-2 _] (re-find git-path-template template-name)
        git-tmp-name  (or git-tmp-name-2 git-tmp-name-1)
        [_ local-root local-tmp-name]     (re-find local-root  template-name)
        clj-only?     (or (and git-url git-tmp-name sha)
                          (and local-root local-tmp-name))
        template-name (cond (and git-url git-tmp-name sha)
                            git-tmp-name
                            (and local-root local-tmp-name)
                            local-tmp-name
                            :else
                            template-name)
        clj-tmp-name  (str template-name "/clj-template")
        clj-version   (cond (and git-url git-tmp-name sha)
                            (cond-> {:git/url git-url :sha sha}
                              git-path (assoc :deps/root git-path))
                            (and local-root local-tmp-name)
                            {:local/root local-root}
                            :else
                            {:mvn/version tmp-version})
        boot-tmp-name (str template-name "/boot-template")
        lein-tmp-name (str template-name "/lein-template")
        all-deps      @basis
        output
        (with-out-str
          (binding [*err* *out*]
            ;; need a modifiable classloader to load runtime dependencies:
            (.setContextClassLoader (Thread/currentThread)
                                    (clojure.lang.RT/makeClassLoader))
            (try
              (resolve-and-load
               all-deps
               {:verbose (and *debug* (> *debug* 1))
                :extra-deps
                {(symbol clj-tmp-name) clj-version}})

              (reset! selected [:clj template-name])
              (catch Exception e
                (when (and *debug* (> *debug* 2))
                  (println "Unable to find clj template:")
                  (stack/print-stack-trace e))
                (reset! failure e)
                (when-not clj-only?
                  (try
                    (resolve-and-load
                     all-deps
                     {:verbose (and *debug* (> *debug* 1))
                      :extra-deps
                      {(symbol boot-tmp-name) {:mvn/version tmp-version}}})

                    (reset! selected [:boot template-name])
                    (catch Exception e
                      (when (and *debug* (> *debug* 2))
                        (println "Unable to find Boot template:")
                        (stack/print-stack-trace e))
                      (reset! failure e)
                      (try
                        (resolve-and-load
                         all-deps
                         {:verbose (and *debug* (> *debug* 1))
                          :extra-deps
                          {(symbol lein-tmp-name) {:mvn/version tmp-version}
                           'leiningen-core {:mvn/version "2.7.1"}
                           'org.sonatype.aether/aether-api {:mvn/version "1.13.1"}
                           'org.sonatype.aether/aether-impl {:mvn/version "1.13.1"}
                           'slingshot {:mvn/version "0.10.3"}}})

                        (reset! selected [:leiningen template-name])
                        (catch Exception e
                          (when (and *debug* (> *debug* 1))
                            (println "Unable to find Leiningen template:")
                            (stack/print-stack-trace e))
                          (reset! failure e))))))))))]
    (if @selected
      (let [sym-name (str (name (first @selected)) ".new." (second @selected))]
        (when (and *debug* (pos? *debug*)
                   output (seq (str/trim output)))
          (println "Output from locating template:")
          (println output))
        (try
          (require (symbol sym-name))
          @selected
          (catch Exception e
            (when (and *debug* (pos? *debug*))
              (println "Unable to require the template symbol:" sym-name)
              (stack/print-stack-trace e)
              (when (> *debug* 1)
                (stack/print-cause-trace e)))
            (throw
             (ex-info
              (format "Could not load template, require of %s failed with: %s%s"
                      sym-name
                      (.getMessage e)
                      (more-debugging))
              {})))))
      (do
        (println output)
        (println "Failed with:" (.getMessage @failure))
        (throw (ex-info
                (format (str "Could not load artifact for template: %s\n"
                             "\tTried coordinates:\n"
                             "\t\t[%s \"%s\"]\n"
                             "\t\t[%s \"%s\"]%s")
                        template-name
                        boot-tmp-name tmp-version
                        lein-tmp-name tmp-version
                        (more-debugging))
                {}))))))

(defn resolve-template
  "Given a template name, resolve it to a symbol (or exit if not possible)."
  [template-name]
  (if-let [[type template-name]
           (try (require (symbol (str "clj.new." template-name)))
                [:clj template-name]
                (catch FileNotFoundException _
                  (resolve-remote-template template-name)))]
    (let [the-ns (str (name type) ".new." template-name)
          fn-name (str/replace template-name #"^.+\." "")]
      (if-let [sym (resolve (symbol the-ns fn-name))]
        sym
        (throw (ex-info (format (str "Found template %s but could not "
                                     "resolve %s/%s within it.%s")
                                template-name
                                the-ns
                                fn-name
                                (more-debugging))
                        {}))))
    (throw (ex-info (format "Could not find template %s on the classpath.%s"
                            template-name
                            (more-debugging))
                    {}))))

(defn- valid-project?
  "Return true if the project name is 'valid': qualified and/or multi-segment."
  [project-name]
  (let [project-sym (try (read-string project-name) (catch Exception _))]
    (or (qualified-symbol? project-sym)
        (and (symbol? project-sym) (re-find #"\." (name project-sym))))))

(defn create*
  "Given a template name, a project name and list of template arguments,
  perform sanity checking on the project name and, if it's sane, then
  generate the project from the template."
  [template-name project-name args]
  (if (valid-project? project-name)
    (apply (resolve-template template-name) project-name args)
    (throw (ex-info
            (let [project-name (or project-name "your-project")]
              (str "Project names must be valid qualified or "
                   "multi-segment Clojure symbols."
                   "\n\nFor example: yourname/" project-name
                   ", yourname." project-name
                   ", or " project-name ".main"))
            {:project-name project-name}))))

(comment
  (binding [*debug*            1
            *use-snapshots?*   false
            *template-version* nil
            bnt/*dir*          nil
            bnt/*force?*       true
            cnt/*dir*          nil
            cnt/*force?*       true
            cnt/*environment*  {}
            lnt/*dir*          nil
            lnt/*force?*       true]
    (create* "compojure" "foo/bar" []))
  nil)

(defn- add-env
  "Add a new SYM=VAL variable to the environment."
  [m k v]
  (let [[sym val] (str/split v #"=")]
    (update-in m [k] assoc (keyword sym) val)))

(def ^:private create-cli
  "Command line argument spec for create command."
  [["-e" "--env SYM=VAL"     "Environment variables" :default {} :assoc-fn add-env]
   ["-f" "--force"           "Force overwrite"]
   ["-h" "--help"            "Provide this help"]
   ["-o" "--output DIR"      "Directory prefix for project creation"]
   ["-?" "--query"           "Display information about what will happen"]
   ["-S" "--snapshot"        "Look for -SNAPSHOT version of the template"]
   ["-v" "--verbose"         "Be verbose; -vvv is very, very verbose!"
    :default 0 :update-fn inc]
   ["-V" "--version VERSION" "Use this version of the template"]])

(defn- create-help []
  (println "Usage:")
  (println "  clojure -m clj-new.create template-name project-name options\n")
  (println "or:")
  (println "  clojure -X clj-new/create :template template-name :name project-name options\n")
  (println "Any additional arguments are passed directly to the template.")
  (println "\nThe template-name may be:")
  (println "* app - create a new application based on deps.edn")
  (println "* lib - create a new library based on deps.edn")
  (println "* template - create a new clj template based on deps.edn")
  (println "\nThe project-name must be a valid Clojure symbol and must either be a")
  (println "qualified name or a multi-segment name (to avoid .core namespaces!).")
  (println "\nThe following options are accepted (as keywords for -X):"))

(defn create-x
  "Project creation entry point with a hash map."
  [{:keys [env force help output query snapshot verbose version ; options
           args ; sequence of arguments to pass to the template
           name template] ; project name and template name
    :or {verbose 0}}]
  (let [name     (some-> name str) ; ensure a string
        template (some-> template str)] ; ensure a string

    (cond (or help (not (and (seq name) (seq template))))
          (let [{:keys [summary]} (cli/parse-opts [] create-cli)]
            (create-help)
            (println summary))

          query
          (if-not (valid-project? name)
            (println "Error:" name "is not a qualified symbol or multi-segment name.")
            (let [project-sym (try (read-string name) (catch Exception _))]
              (println "Will create the folder:"
                       (or output (clojure.core/name project-sym)))
              (println "From the template:" template)
              (when (seq args)
                (println "Passing these arguments:"
                         (str/join " " args)))
              (println "The following substitutions will be used:")
              (binding [cnt/*environment* env]
                (pp/pprint (cnt/project-data name)))))

          :else
          (binding [*debug*            (when (pos? verbose) verbose)
                    *use-snapshots?*   snapshot
                    *template-version* version
                    bnt/*dir*          output
                    bnt/*force?*       force
                    cnt/*dir*          output
                    cnt/*force?*       force
                    cnt/*environment*  env
                    lnt/*dir*          output
                    lnt/*force?*       force]
            (create* template name args))))
  (shutdown-agents))

(defn create
  "Exposed to clj-new command-line with simpler signature."
  [{:keys [args name template]}]
  (let [{:keys [options arguments summary errors]}
        (cli/parse-opts args create-cli)]
    (if (or (:help options) errors)
      (do
        (println "Usage:")
        (println summary)
        (doseq [err errors]
          (println err)))
      (create-x (assoc options
                       :args arguments
                       :name name
                       :template template)))))

(defn generate-code*
  "Given an optional template name, an optional path prefix, a list of
  things to generate (type, type=name), and an optional set of arguments
  for the generator, resolve the template (if provided), and then resolve
  and apply each specified generator."
  [template-name prefix generations args]
  (when template-name (resolve-template template-name))
  (doseq [thing generations]
    (let [[gen-type gen-arg] (str/split thing #"=")
          _ (try (require (symbol (str "clj.generate." gen-type))) (catch Exception _ (println _)))
          generator (resolve (symbol (str "clj.generate." gen-type) "generate"))]
      (if generator
        (apply generator prefix gen-arg args)
        (println (str "Unable to resolve clj.generate."
                      gen-type
                      "/generate -- ignoring: "
                      gen-type
                      (when gen-arg (str "=\"" gen-arg "\""))))))))

(def ^:private generate-cli
  "Command line argument spec for generate command."
  [["-f" "--force"           "Force overwrite"]
   ["-h" "--help"            "Provide this help"]
   ["-p" "--prefix DIR"      "Directory prefix for generation"]
   ["-t" "--template NAME"   "Override the template name"]
   ["-S" "--snapshot"        "Look for -SNAPSHOT version of the template"]
   ["-V" "--version VERSION" "Use this version of the template"]])

(defn- generate-help []
  (println "Usage:")
  (println "  clojure -m clj-new.generate generator options\n")
  (println "or:")
  (println "  clojure -X clj-new/generate :generator [generators] options\n")
  (println "Any additional arguments are passed directly to the generator.")
  (println "\nThe generators may be:")
  (println "* ns=the.ns - generate a new Clojure namespace")
  (println "* file=the.ns body - generate a new file for the namespace with the given body")
  (println "  - an optional argument can specify the extension (clj by default)")
  (println "* defn=the.ns/the-fn - generate a new defn for the-fn within the.ns")
  (println "* def=the.ns/the-sym - generate a new def for the-sym within the.ns")
  (println "  - an optional argument can specify the body (nil by default)")
  (println "* edn=the.ns body - gen a new edn file for the namespace with the given body")
  (println "  - an optional argument can specify the extension (edn by default)")
  (println "Note: you can provide multiple generators when using -X")
  (println "      but only one generator when using -m.")
  (println "\nThe following options are accepted (as keywords for -X):"))

(defn generate-x
  "Project creation entry point with a hash map."
  [{:keys [force help prefix snapshot template version ; options
           args ; sequence of arguments to pass to the generator
           generate] ; sequence of generator spec strings
    :or {prefix "src"}}]
  (if (or help (not (seq generate)))
    (let [{:keys [summary]} (cli/parse-opts [] generate-cli)]
      (generate-help)
      (println summary))

    (binding [cnt/*dir*          "."
              cnt/*force?*       force
              *use-snapshots?*   snapshot
              *template-version* version
              cnt/*overwrite?*   false]
      (generate-code* template prefix generate args)))
  (shutdown-agents))

(defn generate-code
  "Exposed to clj new task with simpler signature."
  [{:keys [args generate]}]
  (let [{:keys [options arguments summary errors]}
        (cli/parse-opts args generate-cli)]
    (if (or (:help options) errors)
      (do
        (println "Usage:")
        (println summary)
        (doseq [err errors]
          (println err)))
      (generate-x (assoc options
                         :args arguments
                         :generate generate)))))
