# clj-new

A work-in-progress that will allow generation of projects from Leiningen or Boot templates, using just the `clj` command-line installation of Clojure.

[![Clojars Project](https://img.shields.io/clojars/v/seancorfield/clj-new.svg)](https://clojars.org/seancorfield/clj-new)

## Getting Started

(not yet fully updated from Boot new version)

Create a basic application:

    clj -Sdeps '{:deps {seancorfield/clj-new {:git/url "https://github.com/seancorfield/clj-new" :sha "???"}}}' -m clj-new.create app myapp
    cd myapp
    boot run # ???

Built-in templates are (to be decided):

* `app` -- A minimal Hello World! application. Comes with `build`, `run`, `test` tasks. `build` creates a runnable "uberjar" in the `target` folder.
* `default` -- A minimal library. Comes with `build`, `test` tasks. `build` installs a JAR of the project into your local Maven cache so you can use `boot watch build` while you're developing to have the latest version always available to other projects.
* `task` -- An example set of Boot tasks. Provides `*-pass-thru`, `*-post`, `*-pre`, `*-simple` task examples for you to build on, as well as `build`, `test` tasks. `build` works as for `default` above.
* `template` -- A minimal Boot template. Provides `build`, `new`, `test` tasks. `build` works as for `default` above. `new` will create a new project based on the template itself.

## General Usage

You can specify a template and a project name:

    clj -Sdeps '{:deps {seancorfield/clj-new {:git/url "https://github.com/seancorfield/clj-new" :sha "???"}}}' -m clj-new.create template-name project-name

(to be rewritten)

`boot-new` will look for `template-name/boot-template` (on Clojars and Maven Central). If it doesn't find a Boot Template (see below), it will look for `template-name/lein-template` instead. `boot-new` should be able to run any existing Leiningen template (if you find one that doesn't work, [please tell me about it](https://github.com/boot-clj/boot-new/issues)!). `boot-new` will then generate a new project folder called `project-name` containing files generated from the specified `template-name`.

If the folder `project-name` already exists, `boot-new` will not overwrite it unless you specify the `-f` / `--force` option. You can override the folder name used with the `-o` / `--to-dir` option. By default, `boot-new` will look for the most recent stable release of the specified template. You can tell it to search for snapshots with the `-S` / `--snapshot` option, and you can specify a particular version to use with the `-V` / `--template-version` option. In general, the long-form option follows the naming used by Leiningen's `new` task for familiarity.

You can pass arguments through to the underlying template with the `-a` / `--args` option (Leiningen uses `--` to separate template arguments from other options but Boot already parses options a little differently).

For a full list of options, ask `new` for help:

    boot -d boot/new new -h

The intent is that all of the basic options from Leiningen's `new` task are supported, along with Boot-specific versions of the built-in templates (`app`, `default`, `task` -- instead of Leiningen's `plugin`, and `template`).

## Boot Templates

(will there need to be clj-templates?)

Boot templates are very similar to Leiningen templates but have an artifact name based on `boot-template` instead of `lein-template` and uses `boot` instead of `leiningen` in all the namespace names. In particular the `boot.new.templates` namespace provides functions such as `renderer` and `->files` that are the equivalent of the ones found in `leiningen.new.templates` when writing a Leiningen Template. The built-in templates are Boot templates, that produce Boot projects.

To develop a new template, run `boot -d boot/new new -t template -n my-template`.  This will generate a project whose `build.boot` has `(def project 'my-template/boot-template)`, with version `0.1.0-SNAPSHOT`. To test it, you will need to build and install it locally (the generated `build.boot` contains a `build` task for this purpose) and then use it to create a project, using either `--snapshot` or `--template-version` so `boot-new` will know which version to use. By default, it looks for the most recent non-SNAPSHOT release on clojars.org.

### Arguments

Previous sections have revealed that it is possible to pass arguments to templates. For multiple arguments, use one `-a` for each argument. For example:

```
# Inside custom-template folder, relying on that template's boot new task.
boot new -t custom-template -n project-name -a arg1 -a arg2 -a arg3
```

These arguments are accessible in the custom-template function as a second argument.

```clj
(defn custom-template
  [name & args]
  (println name " has the following arguments: " args))
```

## Boot Generators

Whereas Boot templates will generate an entire new project in a new directory, Boot generators are intended to add / modify code in an existing project. `boot-new` will run a generator with the `-g type` or `-g type=name` options. The `type` specifies the type of generator to use. The `name` is the main argument that is passed to the generator.

A Boot generator can be part of a project or a template. A generator `foo`, has a `boot.generate.foo/generate` function that accepts at least two arguments, `prefix` and the `name` specified in the `-g` / `--generate` option (which will be `nil` if no `name` was specified -- the generator should validate that). `prefix` specifies the directory in which to perform the code generation and defaults to `src`. It can be overridden with the `-p` / `--prefix` option, but a generator is also free to simply ignore it anyway. In addition, any arguments specified by the `-a` / `--args` option are passed as additional arguments to the generator.

There are currently a few built-in generators:
- `file`
- `ns`
- `def`
- `defn`
- `edn`

The `file` generator creates files relative to the prefix. It optionally accepts a body, file extension, and append? argument.
```bash
boot -d boot/new new -g file=foo.bar -a "(ns foo.bar)" -a "clj"
```

The `ns` generator creates a clojure namespace by using the `file` generator and providing a few defaults.
```bash
boot -d boot/new new -g ns=foo.bar
```

This will generate `src/foo/bar.clj` containing `(ns foo.bar)` (and a placeholder docstring). It will not replace an existing file unless you specify `-f` / `--force` (so `ns` generators are safe-by-default.
```bash
boot -d boot/new new -g defn=foo.bar/my-func
```

If `src/foo/bar.clj` does not exist, it will be generated as a namespace first (using the `ns` generator above), then a definition for `my-func` will be appended to that file (with a placeholder docstring and a dummy argument vector of `[args]`). The generator does not check whether that `defn` already exists so it always appends a new `defn`.

Both the `def` and `defn` generators create files using the `ns` generator above.

The `edn` generator uses the `file` generator internally, with a default extension of `"edn"`.
```bash
boot -d boot/new new -g edn=foo.bar -a "(ns foo.bar)"
```

## Roadmap

* Improve the built-in template `template` so that it can be used to seed a new Boot project.

## License

Copyright © 2016-2018 Sean Corfield and the Leiningen Team for much of the code -- thank you!

Distributed under the Eclipse Public License version 1.0.
