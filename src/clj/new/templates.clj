(ns clj.new.templates
  "clj version of boot.new.templates. Originally derived from
  leiningen.new.templates but modified to have no Boot or Leiningen deps."
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [stencil.core :as stencil])
  (:import (java.util Calendar)))

(defn project-name
  "Returns project name from (possibly group-qualified) name:

  mygroup/myproj => myproj
  myproj         => myproj"
  [s]
  (last (string/split s #"/")))

(defn fix-line-separators
  "Replace all \\n with system specific line separators."
  [s]
  (let [line-sep (if (System/getenv "CLJ_NEW_UNIX_NEWLINES") "\n"
                     (System/getProperty "line.separator"))]
    (string/replace s "\n" line-sep)))

(defn slurp-to-lf
  "Returns the entire contents of the given reader as a single string. Converts
  all line endings to \\n."
  [r]
  (let [sb (StringBuilder.)]
    (loop [s (.readLine r)]
      (if (nil? s)
        (str sb)
        (do
          (.append sb s)
          (.append sb "\n")
          (recur (.readLine r)))))))

(defn slurp-resource
  "Reads the contents of a resource. Temporarily converts line endings in the
  resource to \\n before converting them into system specific line separators
  using fix-line-separators."
  [resource]
  (if (string? resource) ; for 2.0.0 compatibility, can break in 3.0.0
    (-> resource io/resource io/reader slurp-to-lf fix-line-separators)
    (-> resource io/reader slurp-to-lf fix-line-separators)))

(defn sanitize
  "Replace hyphens with underscores."
  [s]
  (string/replace s "-" "_"))

(defn multi-segment
  "Make a namespace multi-segmented by adding another segment if necessary.
  The additional segment defaults to \"core\".

  We remove the leading prefix if it is a public open source repository
  since io.github etc adds very little benefit at the namespace level."
  ([s] (multi-segment s "core"))
  ([s final-segment]
   (let [s (string/replace s #"^(io|com)\.(github|gitlab)\." "")]
     (if (.contains s ".")
       s
       (format "%s.%s" s final-segment)))))

(defn name-to-path
  "Constructs directory structure from fully qualified artifact name:

  \"foo-bar.baz\" becomes \"foo_bar/baz\"

  and so on. Uses platform-specific file separators."
  [s]
  (-> s sanitize (string/replace "." java.io.File/separator)))

(defn sanitize-ns
  "Returns project namespace name from (possibly group-qualified) project name:

  mygroup/myproj  => mygroup.myproj
  myproj          => myproj
  mygroup/my_proj => mygroup.my-proj"
  [s]
  (-> s
      (string/replace "/" ".")
      (string/replace "_" "-")))

(defn raw-group-name
  "Returns group name from (a possibly unqualified) name:

  my.long.group/myproj => my.long.group
  mygroup/myproj       => mygroup
  myproj               => nil"
  [s]
  (let [group-artifact (string/split s #"/")]
    (if (= 2 (count group-artifact))
      (sanitize-ns (first group-artifact))
      (let [grpseq (butlast (string/split (sanitize-ns s) #"\."))]
        (if (seq grpseq)
          (->> grpseq (interpose ".") (apply str))
          s)))))

(defn group-name
  "Return a group name that conforms to Maven/Clojars rules,
  i.e., it looks like a reverse-domain-name. We don't try to
  be very smart about it: if it has a dot in it, and the first
  segment is 2 or 3 characters, we assume it is OK; otherwise
  we'll prefix it with net.clojars.

  Depending on feedback, we may adjust that heuristic."
  [s]
  (let [group (raw-group-name s)
        [tld domain] (string/split group #"\.")]
    (if (and tld domain (<= 2 (count tld) 3))
      group
      (str "net.clojars." group))))

(defn scm-domain
  "Returns the SCM domain from the project name.
  We currently assume github.com if the project name
  has either io.github or com.github in it; similarly
  for gitlab.com. Additional SCM hosts may be supported
  in the future."
  [s]
  (let [[_ _ scm-host] (re-matches #"^(io|com)\.(github|gitlab)\..*$" s)]
    (if scm-host
      (str scm-host ".com")
      "github.com")))

(defn scm-user
  "Returns the SCM username from the project name."
  [s]
  (let [[_ _ _ scm-user] (re-matches #"^(io|com)\.(github|gitlab)\.([^/]*)(/.*|\.[^\.]*)$" s)]
    (or scm-user (raw-group-name s) s)))

(comment
  (for [s ["myproj" "mygroup/myproj" "mygroup/my.proj" "my.group/my.proj" "my.group.proj"
           "io.github.orgname/my.proj" "com.gitlab.orgname.proj" "io.gitlab.orgname/proj"
           "com.acme/everything"]]
    ((juxt identity sanitize-ns group-name project-name
           scm-domain scm-user (comp name-to-path multi-segment sanitize-ns)) s))
  .)

(defn year
  "Get the current year. Useful for setting copyright years and such."
  [] (.get (Calendar/getInstance) Calendar/YEAR))

(defn date
  "Get the current date as a string in ISO8601 format."
  []
  (let [df (java.text.SimpleDateFormat. "yyyy-MM-dd")]
    (.format df (java.util.Date.))))

(def ^:dynamic *environment* {})

(defn project-data
  "Return a standard packet of substitution data for use in a template."
  [name]
  (let [main-ns  (multi-segment (sanitize-ns name))
        username (or (System/getenv "USER")
                  (System/getProperty "user.name"))]
    (merge {:raw-name name
            :name (project-name name)
            :namespace main-ns
            :nested-dirs (name-to-path main-ns)
            :sanitized (sanitize (project-name name))
            :template-nested-dirs "{{nested-dirs}}"
            :group (group-name name)
            :artifact (project-name name)
            :version "0.1.0-SNAPSHOT"
            :user username
            :developer (string/capitalize username)
            :scm-domain (scm-domain name)
            :scm-user (scm-user name)
            :year (year)
            :date (date)}
           *environment*)))

;; It'd be silly to expect people to pull in stencil just to render a mustache
;; string. We can just provide this function instead. In doing so, it is much
;; less likely that template authors will have to pull in any external
;; libraries. Though they are welcome to if they need.
(defn render-text
  [& args]
  (apply stencil/render-string args))

(defn renderer
  "Create a renderer function that looks for mustache templates in the
  right place given the name of your template. If no data is passed, the
  file is simply slurped and the content returned unchanged.

  render-fn - Optional rendering function that will be used in place of the
              default renderer. This allows rendering templates that contain
              tags that conflic with the Stencil renderer such as {{..}}."
  [name & [render-fn]]
  (let [render (or render-fn render-text)]
    (fn [template & [data]]
      (let [path (string/join "/" ["clj" "new" (sanitize name) template])]
        (if-let [resource (io/resource path)]
          (if data
            (render (slurp-resource resource) data)
            (io/reader resource))
          (throw (ex-info (format "Template resource '%s' not found." path) {})))))))

(defn raw-resourcer
  "Create a renderer function that looks for raw files in the
  right place given the name of your template."
  [name]
  (fn [file]
    (let [path (string/join "/" ["clj" "new" (sanitize name) file])]
      (if-let [resource (io/resource path)]
        (io/input-stream resource)
        (throw (ex-info (format "File '%s' not found." path) {}))))))

;; Our file-generating function, `->files` is very simple. We'd like
;; to keep it that way. Sometimes you need your file paths to be
;; templates as well. This function just renders a string that is the
;; path to where a file is supposed to be placed by a template.
;; It is private because you shouldn't have to call it yourself, since
;; `->files` does it for you.
(defn- template-path [name path data]
  (io/file name (render-text path data)))

(def ^{:dynamic true} *dir* nil)
(def ^{:dynamic true} *force?* false)
(def ^{:dynamic true} *overwrite?* true)

(defn project-dir
  "Return the generated project directory (as a path string)."
  [name]
  (or *dir*
      (-> (System/getProperty "user.dir")
          (io/file name) (.getPath))))

;; A template, at its core, is meant to generate files and directories that
;; represent a project. This is our way of doing that. `->files` is basically
;; a mini-DSL for generating files. It takes your mustache template data and
;; any number of vectors or strings. It iterates through those arguments and
;; when it sees a vector, it treats the first element as the path to spit to
;; and the second element as the contents to put there. If it encounters a
;; string, it treats it as an empty directory that should be created. Any parent
;; directories for any of our generated files and directories are created
;; automatically. All paths are considered mustache templates and are rendered
;; with our data. Of course, this doesn't effect paths that don't have templates
;; in them, so it is all transparent unless you need it.
(defn ->files
  "Generate a file with content. path can be a java.io.File or string.
  It will be turned into a File regardless. Any parent directories will be
  created automatically. Data should include a key for :name so that the project
  is created in the correct directory."
  [{:keys [name] :as data} & paths]
  (let [dir (project-dir name)]
    (if (or (= "." dir) (.mkdir (io/file dir)) *force?*)
      (doseq [path (remove nil? paths)]
        (if (string? path)
          (.mkdirs (template-path dir path data))
          (let [[path content & options] path
                path (template-path dir path data)
                options (apply hash-map options)]
            (.mkdirs (.getParentFile path))
            (cond (not (.exists path))
                  (io/copy content path)

                  (:append options)
                  (with-open [w (io/writer path :append true)]
                    (io/copy content w))

                  (or *overwrite?* *force?* (:overwrite options))
                  (io/copy content path)

                  :else
                  (println (str path " exists."
                                " Use -f / --force to overwrite it.")))
            (when (:executable options)
              (.setExecutable path true)))))
      (println (str "Could not create directory " dir
                    ". Maybe it already exists?"
                    "  Use -f / --force to overwrite it.")))))
