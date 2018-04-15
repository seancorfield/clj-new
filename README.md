# clj-new

Generate new projects from Leiningen or Boot templates, or `clj-template` projects, using just the `clj` command-line installation of Clojure!

## Getting Started

You'll probably want to add `clj-new` as an alias in your `~/.clojure/deps.edn` like this:

    {:aliases
     {:new {:extra-deps {seancorfield/clj-new
                         {:git/url "https://github.com/seancorfield/clj-new"
                          :sha "025f678af79c460e23f762aeb33dbb4af16f7ec7"}}
            :main-opts ["-m" "clj-new.create"]}}
     ...}

Create a basic application:

    clj -A:new app myname/myapp
    cd myapp
    clj -m myname.myapp

Run the tests:

    clj -A:test:runner

Built-in templates are:

* `app` -- A minimal Hello World! application with `deps.edn`. Can run it via `clj -m` and can test it with `clj -A:test:runner`.
* `lib` -- A minimal library with `deps.edn`. Can test it with `clj -A:test:runner`.
* `template` -- A minimal `clj-new` template. Can test it with `clj -A:test:runner`. Can produce a new template with `clj -m clj-new.create mytemplate myname/mynewapp` (where `mytemplate` is the appropriate part of whatever project name you used when you asked `clj-new` to create the template project).

The project name should be a qualified Clojure symbol, where the first part is typically your GitHub account name or your organization's domain reversed, e.g., `com.acme`, and the second part is the "local" name for your project (and is used as the name of the folder in which the project is created). An alternative is to use a multi-segment project name, such as `foo.bar` (the folder created will be called `foo.bar` and will contain `src/foo/bar.clj`).

## General Usage

The general form of the command is:

    clj -A:new template-name project-name arg1 arg2 arg3 ...

If `template-name` is not one of the built-in ones (or is not already on the classpath), this will look for `template-name/clj-template` (on Clojars and Maven Central). If it doesn't find a `clj` template, it will look for `template-name/boot-template` instead. If it doesn't find a Boot template, it will look for `template-name/lein-template` instead. `clj-new` should be able to run any existing Leiningen or Boot templates (if you find one that doesn't work, [please tell me about it](https://github.com/seancorfield/clj-new/issues)!). `clj-new` will then generate a new project folder based on the `project-name` containing files generated from the specified `template-name`.

Alternatively, `template-name` can be a `:git/url` and `:sha` like this:

    clj -A:new https://github.com/somename/someapp@c1fc0cdf5a21565676003dbc597e380467394a89 project-name arg1 arg2 arg3 ...

In this case, `clj.new.someapp` must exist in the template and `clj.new.someapp/someapp` will be invoked to generate the template.

Or, `template-name` can be a `:local/root` and template name like this:

    clj -A:new /path/to/clj-template::new-app project-name arg1 arg2 arg3 ...

In this case, `clj.new.new-app` must exist in the template and `clj.new.new-app/new-app` will be invoked to generate the template.

If the folder for `project-name` already exists, `clj-new` will not overwrite it (an option to force overwriting may be added).

Any arguments after the `project-name` are passed directly to the template (`arg1`, `arg2`, `arg3`, ... above).

Note: not all Leiningen or Boot templates accept a qualified `project-name` so you may have to use a multi-segment name instead, e.g., `project.name`.

## `clj` Templates

`clj` templates are very similar to Leiningen and Boot templates but have an artifact name based on `clj-template` instead of `lein-template` or `boot-template` and use `clj` instead of `leiningen` or `boot` in all the namespace names. In particular the `clj.new.templates` namespace provides functions such as `renderer` and `->files` that are the equivalent of the ones found in `leiningen.new.templates` when writing a Leiningen Template (or `boot.new.templates` when writing a Boot Template). The built-in templates are `clj` templates, that produce `clj` projects with `deps.edn` files.

### Arguments

Previous sections have revealed that it is possible to pass arguments to templates. For example:

```
# Inside custom-template folder, relying on that template's clj-new dependency.
clj -m clj-new.create custom-template project-name arg1 arg2 arg3
```

These arguments are accessible in the `custom-template` function as a second argument.

```clj
(defn custom-template
  [name & args]
  (println name " has the following arguments: " args))
```

## clj Generators

Whereas clj templates will generate an entire new project in a new directory, clj generators are intended to add / modify code in an existing project. `clj -m clj-new.generate` will run a generator with an argument for the `type` or `type=name` options. The `type` specifies the type of generator to use. The `name` is the main argument that is passed to the generator.

A clj generator can be part of a project or a template. A generator `foo`, has a `clj.generate.foo/generate` function that accepts at least two arguments, `prefix` and the `name` specified as the main argument. `prefix` specifies the directory in which to perform the code generation and defaults to `src` (it cannot currently be overridden). In addition, any additional arguments are passed as additional arguments to the generator.

There are currently a few built-in generators:
- `file`
- `ns`
- `def`
- `defn`
- `edn`

The `file` generator creates files relative to the prefix. It optionally accepts a body, and file extension. Those default to `nil` and `"clj"` respectively.
```bash
clj -m clj-new.generate file=foo.bar "(ns foo.bar)" "clj"
```

The `ns` generator creates a clojure namespace by using the `file` generator and providing a few defaults.
```bash
clj -m clj-new.generate ns=foo.bar
```

This will generate `src/foo/bar.clj` containing `(ns foo.bar)` (and a placeholder docstring). It will not replace an existing file.
```bash
clj -m clj-new.generate defn=foo.bar/my-func
```

If `src/foo/bar.clj` does not exist, it will be generated as a namespace first (using the `ns` generator above), then a definition for `my-func` will be appended to that file (with a placeholder docstring and a dummy argument vector of `[args]`). The generator does not check whether that `defn` already exists so it always appends a new `defn`.

Both the `def` and `defn` generators create files using the `ns` generator above.

The `edn` generator uses the `file` generator internally, with a default extension of `"edn"`.
```bash
clj -m clj-new.generate edn=foo.bar "(ns foo.bar)"
```

## Roadmap

* Improve the built-in template `template` so that it can be used to seed a new `clj` project.

## License

Copyright © 2016-2018 Sean Corfield and the Leiningen Team for much of the code -- thank you!

Distributed under the Eclipse Public License version 1.0.
