;; copyright (c) 2020-2021 sean corfield, all rights reserved

(ns clj-new
  "Provides an API suitable for use with the Clojure CLI's `-X` option."
  {:org.babashka/cli {:collect {:args []}
                      :coerce {:verbose :long}}}
  (:require [clj-new.helpers :as h]))

(defn create
  "Public API for clojure -X usage.

The following `:exec-args` can be provided for `clj-new/create`:

* `:name` -- the name of the project (as a symbol or a string); required; must be a qualified project name or a multi-segment dotted project name
* `:template` -- the name of the template to use (as a symbol or a string); required
* `:args` -- an optional vector of strings (or symbols) to pass to the template itself as command-line argument strings
* `:edn-args` -- an optional EDN expression to pass to the template itself as the arguments for the template; takes precedence over `:args`; nearly all templates expect a sequence of strings so `:args` is going to be the easiest way to pass arguments
* `:env` -- a hash map of additional variable substitutions in templates (see [The Generated `pom.xml` File](#the-generated-pomxml-file) below for a list of \"built-in\" variables that can be overridden)
* `:force` -- if `true`, will force overwrite the target directory if it exists
* `:help` -- if `true`, will provide a summary of these options as help
* `:output` -- specify the project directory to create (the default is to use the project name as the directory)
* `:query` -- if `true`, instead of actually looking up the template and generating the project, output an explanation of what `clj-new` will try to do
* `:snapshot` -- if `true`, look for -SNAPSHOT version of the template (not just a release version)
* `:verbose` -- 1, 2, or 3, indicating the level of debugging in increasing detail
* `:version` -- use this specific version of the template, e.g., `'\"1.2.3\"'` (remember that strings need to be 'quoted' as exec args)
  "
  [options]
  (h/create-x options))

(defn app
  "Create new app project."
  [options]
  (h/create-x (assoc options :template "app")))

(defn lib
  "Create new lib project."
  [options]
  (h/create-x (assoc options :template "lib")))

(defn polylith
  "Create new Polylith project."
  [options]
  (h/create-x (assoc options :template "polylith")))

(defn template
  "Create new template project."
  [options]
  (h/create-x (assoc options :template "template")))

(defn generate
  "Public API for clojure -X usage."
  [options]
  (h/generate-x options))
