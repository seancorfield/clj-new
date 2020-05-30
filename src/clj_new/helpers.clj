(ns clj-new.helpers
  "The top-level logic for the clj-new create/generate entry points."
  (:require [clojure.pprint :as pp]
            [clojure.stacktrace :as stack]
            [clojure.string :as str]
            [clojure.tools.cli :as cli]
            [clojure.tools.deps.alpha :as deps]
            [clojure.tools.deps.alpha.reader :refer [default-deps
                                                     read-deps]]
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
        all-deps      (read-deps (default-deps))
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
    (when (and *debug* (pos? *debug*)
               output (seq (str/trim output)))
      (println "Output from locating template:")
      (println output))
    (if @selected
      (let [sym-name (str (name (first @selected)) ".new." (second @selected))]
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
                      (if *debug*
                        (if (< *debug* 3)
                          (str "\n\nFor more detail, increase verbose logging with "
                               (case *debug*
                                 0 "-v, -vv, or -vvv"
                                 1 "-vv or -vvv"
                                 2 "-vvv"))
                          "")
                        "\n\nFor more detail, enable verbose logging with -v, -vv, or -vvv"))
              {})))))
      (do
        (println output)
        (println "Failed with:" (.getMessage @failure))
        (throw (ex-info
                (format (str "Could not load artifact for template: %s\n"
                             "\tTried coordinates:\n"
                             "\t\t[%s \"%s\"]\n"
                             "\t\t[%s \"%s\"]")
                        template-name
                        boot-tmp-name tmp-version
                        lein-tmp-name tmp-version) {}))))))

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
                                     "resolve %s/%s within it.")
                                template-name
                                the-ns
                                fn-name) {}))))
    (throw (ex-info (format "Could not find template %s on the classpath."
                            template-name) {}))))

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
    (throw (ex-info "Project names must be valid qualified or multi-segment Clojure symbols."
                    {:project-name project-name}))))

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

(defn create
  "Exposed to clj-new command-line with simpler signature."
  [{:keys [args name template]}]
  (let [{:keys [options arguments summary errors]}
        (cli/parse-opts args create-cli)]
    (cond (or (:help options) errors)
          (do
            (println "Usage:")
            (println summary)
            (doseq [err errors]
              (println err)))
          (:query options)
          (if-not (valid-project? name)
            (println "Error:" name "is not a qualified symbol or multi-segment name.")
            (let [project-sym (try (read-string name) (catch Exception _))]
              (println "Will create the folder:"
                       (or (:output options)
                           (clojure.core/name project-sym)))
              (println "From the template:" template)
              (when (seq arguments)
                (println "Passing these arguments:"
                         (str/join " " arguments)))
              (println "The following substitutions will be used:")
              (binding [cnt/*environment* (:env options)]
                (pp/pprint (cnt/project-data name)))))
          :else
          (let [{:keys [env force snapshot version output verbose]} options]
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
              (create* template name arguments))))))

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
      (let [{:keys [force prefix snapshot template version]} options]
        (binding [cnt/*dir*          "."
                  cnt/*force?*       force
                  *use-snapshots?*   snapshot
                  *template-version* version
                  cnt/*overwrite?*   false]
          (generate-code* template (or prefix "src") generate arguments))))))
