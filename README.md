# clj-new

Generate new projects from Leiningen or Boot templates, or `clj-template` projects, using just the `clj` command-line installation of Clojure!

## Getting Started

You can use this from the command line...

```bash
clj -Sdeps '{:deps
              {seancorfield/clj-new
                {:mvn/version "0.9.0"}}}' \
  -m clj-new.create \
  app \
  myname/myapp
```

...but you'll probably want to add `clj-new` as an alias in your `~/.clojure/deps.edn` like this:

```clj
    {:aliases
     {:new {:extra-deps {seancorfield/clj-new
                         {:mvn/version "0.9.0"}}
            :main-opts ["-m" "clj-new.create"]}}
     ...}
```

Create a basic application:

```bash
    clj -A:new app myname/myapp
    cd myapp
    clj -m myname.myapp
```

Run the tests:

```bash
    clj -A:test:runner
```

### Templates

Built-in templates are:

* `app` -- A minimal Hello World! application with `deps.edn`. Can run it via `clj -m` and can test it with `clj -A:test:runner`.
* `lib` -- A minimal library with `deps.edn`. Can test it with `clj -A:test:runner`.
* `template` -- A minimal `clj-new` template. Can produce a new template with `clj -m clj-new.create mytemplate myname/mynewapp` (where `mytemplate` is the appropriate part of whatever project name you used when you asked `clj-new` to create the template project).

The project name should be a qualified Clojure symbol, where the first part is typically your GitHub account name or your organization's domain reversed, e.g., `com.acme`, and the second part is the "local" name for your project (and is used as the name of the folder in which the project is created), e.g., `com.acme/my-cool-project`. This will create a folder called `my-cool-project` and the main namespace for the new project will be `com.acme.my-cool-project`, so the file will be `src/com/acme/my_cool_project.clj`.

An alternative is to use a multi-segment project name, such as `com.acme.another-project`. This will create a folder called `com.acme.another-project` (compared to above, which just uses the portion after the `/`). The main namespace will be `com.acme.another-project` in `src/com/acme/another_project.clj`, similar to the qualified project name above.

#### The `app` Template

The generated project is an application. It has a `-main` function in the main project
namespace, with a `(:gen-class)` class in the `ns` form. In addition to being able to
run the project directly (with `clojure -m myname.myapp`) and run the tests, you can
also build an uberjar for the project with `clojure -A:uberjar`, which you can then
run with `java -jar myapp`.

#### The `lib` Template

The generated project is a library. It has no `-main` function. In addition to
being able to run the tests, you can also build a jar file for deployment
with `clojure -A:jar`. You will probably need to adjust some of the information
inside the generated `pom.xml` file before deploying the jar file.

Once you've updated the `pom.xml` file, you can install it locally with
`clojure -A:install` or deploy it to Clojars with `clojure -A:deploy`. For
that you need these environment variables set:

* `CLOJARS_USERNAME` -- your Clojars username
* `CLOJARS_PASSWORD` -- your Clojars password

#### The `template` Template

The generated project is a very minimal `clj-template`. It has no `-main`
function and has no tests. You can however build a jar file for deployment
with `clojure -A:jar`. You will probably need to adjust some of the information
inside the generated `pom.xml` file before deploying the jar file.

> Note: when you create a template project called myname/mytemplate, you will get a folder called `mytemplate` and the `pom.xml` file will specify the group/artifact as `mytemplate/clj-template` which is the convention expected by `clj-new`.

As with the `lib` template, once you've updated the `pom.xml` file, you can
install it locally or deploy it to Clojars, via the appropriate aliases.

#### The Generated `pom.xml` File

Each of the built-in templates produces a project that contains a `pom.xml`
file, which is used to build the uberjar (`app`) or jar file (`lib` and `template`),
as well as guide the deployment of the latter two.

The goal is that if you used an appropriate `myname/myapp` style name for the
project that you asked `clj-new` to create, then most of the fields in the
`pom.xml` file should be usable as-is.

You can override the default value of several fields in the `pom.xml` file
using the `-e` option to `clj-new.create`:

* `group` -- defaults to the `myname` portion of `myname/myapp`,
* `artifact` -- defaults to the `myapp` portion of `myname/myapp`,
* `version` -- defaults to `"0.1.0-SNAPSHOT"`,
* `description` -- defaults to `"FIXME: my new ..."` (`application`, `library`, or `template`),
* `developer` -- defaults to a capitalized version of your computer's logged in username.

The `description` field is also used in the generated project's `README.md` file.

#### General Usage

The general form of the command is:

    clj -A:new template-name project-name arg1 arg2 arg3 ...

As noted above, `project-name` should be a qualified symbol, such as `mygithubusername/my-new-project`, or a multi-segment symbol, such as `my.cool.project`. Some templates will not work with the former but it is recommended you try that format first.

If `template-name` is not one of the built-in ones (or is not already on the classpath), this will look for `template-name/clj-template` (on Clojars and Maven Central). If it doesn't find a `clj` template, it will look for `template-name/boot-template` instead. If it doesn't find a Boot template, it will look for `template-name/lein-template` instead. `clj-new` should be able to run any existing Leiningen or Boot templates (if you find one that doesn't work, [please tell me about it](https://github.com/seancorfield/clj-new/issues)!). `clj-new` will then generate a new project folder based on the `project-name` containing files generated from the specified `template-name`. It does that by requiring `clj.new.<template-name>` (or `boot.new.<template-name>` or `leiningen.new.<template-name>`) and invoking the `<template-name>` function inside that namespace, passing in `<project-name>` and those arguments from the command line.

Alternatively, `template-name` can be a `:git/url` and `:sha` like this:

    clj -A:new https://github.com/somename/someapp@c1fc0cdf5a21565676003dbc597e380467394a89 project-name arg1 arg2 arg3 ...

In this case, `clj.new.someapp` must exist in the template and `clj.new.someapp/someapp` will be invoked to generate the template. A GitHub repository may include multiple templates, so you can also use this form:

    clj -A:new https://github.com/somename/somerepo/someapp@c1fc0cdf5a21565676003dbc597e380467394a89 project-name arg1 arg2 arg3 ...

`somename/somerepo` here contains templates in subdirectories, including `someapp`. Again, `clj.new.someapp` must exist in the template in that subdirectory and `clj.new.someapp/someapp` will be invoked to generate the template.

Or, `template-name` can be a `:local/root` and template name like this:

    clj -A:new /path/to/clj-template::new-app project-name arg1 arg2 arg3 ...

In this case, `clj.new.new-app` must exist in the template and `clj.new.new-app/new-app` will be invoked to generate the template.

If the folder for `project-name` already exists, `clj-new` will not overwrite it unless you specify the `-f` / `--force` option.

Any arguments after the `project-name` are parsed using `tools.cli` for flags, and any non-flag arguments are passed directly to the template (`arg1`, `arg2`, `arg3`, ... above).

Flag arguments for `clj-new.create` are:
* `-e` or `--env` -- accepts `sym=val` to add `{:sym "val"}` as additional variable substitutions in templates; can be used to provide new variables or override existing ones (new in 0.8.0)
* `-f` or `--force` -- will force overwrite the target directory if it exists
* `-h` or `--help` -- will provide a summary of these options as help
* `-o` or `--output`, followed by a directory path -- specify the project directory to create (the default is to use the project name as the directory)
* `-?` or `--query` -- instead of actually looking up the template and generating the project, output an explanation of what `clj-new` will try to do (new in 0.8.0)
* `-S` or `--snapshot` -- look for -SNAPSHOT version of the template (not just a release version)
* `-v` or `--verbose` -- enable debugging -- be verbose!
* `-V` or `--version`, followed by a version -- use this specific version of the template

Note: not all Leiningen or Boot templates accept a qualified `project-name` so you may have to use a multi-segment name instead, e.g., `project.name`.

#### Example Usage

Here are some examples, generating projects from existing templates:

```
clj -A:new luminus yourname/example.webapp -o mywebapp +http-kit +h2 +reagent +auth
```

This creates a folder called `mywebapp` with a Luminus web application that will use `http-kit`, the H2 database, the Reagent ClojureScript library, and the Buddy library for authentication. The `-main` function is in `yourname.example.webapp.core`, which is in the  `mywebapp/src/clj/yourname/example/webapp/core.clj` file. Note that the [Luminus template](https://github.com/luminus-framework/luminus-template) produces a Leiningen-based project, not a CLI/`deps.edn` one, but you can also tell it to produce a Boot-based project (with `+boot`).

```
clj -A:new re-frame yourname/spa -o front-end +garden +10x +routes
```

This creates a folder called `front-end` with a ClojureScript Single Page Application that uses Garden for CSS, `re-frame-10x` for debugging, and Secretary for routing. The entry point is in the `yourname.spa.core` namespace which is in the `front-end/src/cljs/yourname/spa/core.cljs` file. As with Luminus, the [`re-frame` template](https://github.com/day8/re-frame-template) produces a Leiningen-based project, not a CLI/`deps.edn` one.

```
clj -A:new electron-app yourname/example
```

This creates a folder called `example` with a skeleton Electron application, using Figwheel and Reagent. The entry point is in the `example.main.core` namespace which is in the `example/src/main/example/main/core.cljs` file. This [Electron template](https://github.com/paulbutcher/electron-app) produces a CLI/`deps.edn`-based project.

#### `clj` Templates

`clj` templates are very similar to Leiningen and Boot templates but have an artifact name based on `clj-template` instead of `lein-template` or `boot-template` and use `clj` instead of `leiningen` or `boot` in all the namespace names. In particular the `clj.new.templates` namespace provides functions such as `renderer` and `->files` that are the equivalent of the ones found in `leiningen.new.templates` when writing a Leiningen Template (or `boot.new.templates` when writing a Boot Template). The built-in templates are `clj` templates, that produce `clj` projects with `deps.edn` files.

If your template name is `foo-bar`, then you should have `clj.new.foo-bar` as the main namespace and it should contain a `foo-bar` function that will render the template:

```clj
;; src/clj/new/foo_bar.clj:
(ns clj.new.foo-bar ,,,)

(defn foo-bar
  "Generate a cool new foo bar project!"
  [name & args]
  ,,,)
```

When you publish it to Clojars, it should have a group ID matching the template name and an artifact ID of `clj-template`: `foo-bar/clj-template`. If you expect people to depend on the template via GitHub, you should also name the repo `foo-bar` so that `https://github.com/<username>/foo-bar` is the `:git/url` people will use.

#### Arguments

Previous sections have revealed that it is possible to pass arguments to templates. For example:

```
clj -A:new custom-template project-name arg1 arg2 arg3
```

These arguments are accessible in the `custom-template` function as a second argument.

```clj
(ns clj.new.custom-template ,,,)

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
# Inside project folder, relying on the clj-new dependency.
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

Any arguments after `type=name` are parsed using `tools.cli` for flags, and any non-flag arguments are passed directly to the generator.

Flag arguments for `clj-new.generate` are:
* `-f` or `--force` -- will force overwrite the target directory/file if it exists
* `-h` or `--help` -- will provide a summary of these options as help
* `-p` or `--prefix`, followed by a directory path -- specify the project directory in which to run the generator (the default is `src` but `-p .` will allow a generator to modify files in the root of your project)
* `-S` or `--snapshot` -- look for -SNAPSHOT version of the template (not just a release version)
* `-t` or `--template`, followed by a template name -- load this template (using the same rules as for `clj-new.create` above) and then run the specified generator
* `-V` or `--version`, followed by a version -- use this specific version of the template

## Roadmap

* Improve the built-in template `template` so that it can be used to seed a new `clj` project.

## License

Copyright © 2016-2020 Sean Corfield and the Leiningen Team for much of the code -- thank you!

Distributed under the Eclipse Public License version 1.0.
