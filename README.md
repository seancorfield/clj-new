# clj-new

Generate new projects from Leiningen or Boot templates, or `clj-template` projects, using just the `clj` command-line installation of Clojure!

For support, help, general questions, use the [#clj-new channel on the Clojurians Slack](https://app.slack.com/client/T03RZGPFR/C019ZQSPYG6).

## Getting Started

> Note: these instructions assume you are using the Clojure CLI version 1.10.1.727 or later!

The easiest way to use `clj-new` is by adding an alias to your `~/.clojure/deps.edn` file (or `~/.config/clojure/deps.edn` file) like this:

```clj
    {:aliases
     {:new {:extra-deps {seancorfield/clj-new
                         {:mvn/version "1.1.234"}}
            :ns-default clj-new
            :exec-args {:template "app"}}}
     ...}
```

Now you can create a basic application:

```bash
    clojure -X:new create :name myname/myapp
    cd myapp
    clojure -M -m myname.myapp
```

Run the tests:

```bash
    clojure -M:test:runner
```

or you can create a basic library:

```bash
    clojure -X:new create :template lib :name myname/mylib
    cd mylib
```

Run the tests:

```bash
    clojure -M:test:runner
```

If you think you are going to be creating more libraries than applications, you could specify `:template "lib"` in the `:exec-args` hash map, to specify the default. Or you could provide different aliases, such as:

```clj
    {:aliases
     {:new-app {:extra-deps {seancorfield/clj-new
                             {:mvn/version "1.1.234"}}
                :exec-fn clj-new/create
                :exec-args {:template "app"}}
      :new-lib {:extra-deps {seancorfield/clj-new
                             {:mvn/version "1.1.234"}}
                :exec-fn clj-new/create
                :exec-args {:template "lib"}}}
     ...}
```

Now you can use those as follows:

```bash
    clojure -X:new-app :name myname/myapp
    clojure -X:new-lib :name myname/mylib
```

The following `:exec-args` can be provided for `clj-new/create`:

* `:name` -- the name of the project (as a symbol or a string); required; must be a qualified project name or a multi-segment dotted project name
* `:template` -- the name of the template to use (as a symbol or a string); required
* `:args` -- an optional vector of string to pass to the template itself as command-line arguments
* `:env` -- a hash map of additional variable substitutions in templates
* `:force` -- if `true`, will force overwrite the target directory if it exists
* `:help` -- if `true`, will provide a summary of these options as help
* `:output` -- specify the project directory to create (the default is to use the project name as the directory)
* `:query` -- if `true`, instead of actually looking up the template and generating the project, output an explanation of what `clj-new` will try to do
* `:snapshot` -- if `true`, look for -SNAPSHOT version of the template (not just a release version)
* `:verbose` -- 1, 2, or 3, indicating the level of debugging in increasing detail
* `:version` -- use this specific version of the template

> Note: Unlike Leiningen, `clj-new` requires that you use either a qualified
name for your project, such as `<username>/<project-name>` or
`<org-name>/<project-name>` (e.g., your GitHub username or organization name),
or a dotted name, such as `my.project`. Leiningen's default behavior, of adding
`.core` to a single segment name such as `foo`, can be achieved with
`clj -X:new create :template lib :name foo.core`. Although very common in older Clojure projects, the
use of a `core` namespace is really just a historical accident because it was
Leiningen's default behavior!

### Templates

Built-in templates are:

* `app` -- A minimal Hello World! application with `deps.edn`. Can run it via `clj -M -m` and can test it with `clj -M:test:runner`.
* `lib` -- A minimal library with `deps.edn`. Can test it with `clj -M:test:runner`.
* `template` -- A minimal `clj-new` template.

> Note: you can find third-party templates on Clojars using these searches [`<template-name>/clj-template`](https://clojars.org/search?q=artifact-id:clj-template), [`<template-name>/lein-template`](https://clojars.org/search?q=artifact-id:lein-template) or [`<template-name>/boot-template`](https://clojars.org/search?q=artifact-id:boot-template).

The project name should be a qualified Clojure symbol, where the first part is typically your GitHub account name or your organization's domain reversed, e.g., `com.acme`, and the second part is the "local" name for your project (and is used as the name of the folder in which the project is created), e.g., `com.acme/my-cool-project`. This will create a folder called `my-cool-project` and the main namespace for the new project will be `com.acme.my-cool-project`, so the file will be `src/com/acme/my_cool_project.clj`. In the generated `pom.xml` file, the group ID will be `com.acme` and the artifact ID will be `my-cool-project` -- following this pattern means you are already set up for publishing to Clojars (or some other Maven-like repository).

An alternative is to use a multi-segment project name, such as `com.acme.another-project`. This will create a folder called `com.acme.another-project` (compared to above, which just uses the portion after the `/`). The main namespace will be `com.acme.another-project` in `src/com/acme/another_project.clj`, similar to the qualified project name above. In the generated `pom.xml` file, the group ID will be the "stem" of the project name (`com.acme`) and the artifact ID will be the full project name (`com.acme.another-project`) -- again, you'll be set up for publishing to Clojars etc, but be aware of the difference between how dotted names and qualified names affect the generated project.

You can, of course, modify the generated `pom.xml` file to have whatever group and artifact ID you want, if you don't like these defaults.

#### The `app` Template

The generated project is an application. It has a `-main` function in the main project
namespace, with a `(:gen-class)` class in the `ns` form. In addition to being able to
run the project directly (with `clojure -M -m myname.myapp`) and run the tests, you can
also build an uberjar for the project with `clojure -M:uberjar`, which you can then
run with `java -jar myapp`.

#### The `lib` Template

The generated project is a library. It has no `-main` function. In addition to
being able to run the tests, you can also build a jar file for deployment
with `clojure -M:jar`. You will probably need to adjust some of the information
inside the generated `pom.xml` file before deploying the jar file.

Once you've updated the `pom.xml` file, you can install it locally with
`clojure -M:install` or deploy it to Clojars with `clojure -M:deploy`. For
that you need these environment variables set:

* `CLOJARS_USERNAME` -- your Clojars username
* `CLOJARS_PASSWORD` -- your Clojars password

#### The `template` Template

The generated project is a very minimal `clj-template`. It has no `-main`
function and has no tests. You can however build a jar file for deployment
with `clojure -M:jar`. You will probably need to adjust some of the information
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
using the `:env` exec-arg to `clj-new/create` as a hash map:

* `:group` -- defaults to the `myname` portion of `myname/myapp`,
* `:artifact` -- defaults to the `myapp` portion of `myname/myapp`,
* `:version` -- defaults to `"0.1.0-SNAPSHOT"`,
* `:description` -- defaults to `"FIXME: my new ..."` (`application`, `library`, or `template`),
* `:developer` -- defaults to a capitalized version of your computer's logged in username.
* `:scm-domain` -- defaults to `github.com`; used in all the SCM links in the generated projects: `https://{{scm-domain}}/{{group}}/{{artifact}}`

The `:description` field is also used in the generated project's `README.md` file.

Example:

```bash
    clojure -X:new-app :name myname/myapp \
      :env '{:group myusername :artifact my-cool-app :version "1.2.3"}'
```

This creates the same project structure as in the earlier `myname/myapp` example except that the generated `pom.xml` file will contain:

```xml
  <groupId>myusername</groupId>
  <artifactId>my-cool-app</artifactId>
  <version>1.2.3</version>
  <description>FIXME: my new application.</description>
  <url>https://github.com/myusername/my-cool-app</url>
```

#### General Usage

The general form of the command is:

    clojure -X:new create :template template-name :name project-name :args '[arg1 arg2 arg3 ...]'

As noted above, `project-name` should be a qualified symbol, such as `mygithubusername/my-new-project`, or a multi-segment symbol, such as `my.cool.project`. Some templates will not work with the former but it is recommended you try that format first.

If `template-name` is not one of the built-in ones (or is not already on the classpath), this will look for `template-name/clj-template` (on Clojars and Maven Central). If it doesn't find a `clj` template, it will look for `template-name/boot-template` instead. If it doesn't find a Boot template, it will look for `template-name/lein-template` instead. `clj-new` should be able to run any existing Leiningen or Boot templates (if you find one that doesn't work, [please tell me about it](https://github.com/seancorfield/clj-new/issues)!). `clj-new` will then generate a new project folder based on the `project-name` containing files generated from the specified `template-name`. It does that by requiring `clj.new.<template-name>` (or `boot.new.<template-name>` or `leiningen.new.<template-name>`) and invoking the `<template-name>` function inside that namespace, passing in `<project-name>` and those arguments from the command line.

Alternatively, `template-name` can be a `:git/url` and `:sha` like this:

    clj -X:new create :template '"https://github.com/somename/someapp@c1fc0cdf5a21565676003dbc597e380467394a89"' \
      :name project-name :args '[arg1 arg2 arg3 ...]'

In this case, `clj.new.someapp` must exist in the template and `clj.new.someapp/someapp` will be invoked to generate the template. A GitHub repository may include multiple templates, so you can also use this form:

    clj -X:new create :template '"https://github.com/somename/somerepo/someapp@c1fc0cdf5a21565676003dbc597e380467394a89"' \
      :name project-name :args '[arg1 arg2 arg3 ...]'

`somename/somerepo` here contains templates in subdirectories, including `someapp`. Again, `clj.new.someapp` must exist in the template in that subdirectory and `clj.new.someapp/someapp` will be invoked to generate the template.

Or, `template-name` can be a `:local/root` and template name like this:

    clj -X:new create :template '"/path/to/clj-template::new-app"' \
      :name project-name :args '[arg1 arg2 arg3 ...]'

In this case, `clj.new.new-app` must exist in the template and `clj.new.new-app/new-app` will be invoked to generate the template.

> Note: since the `:git/url` and `:local/root` forms of `:template` cannot be provided as Clojure symbols, they must be provided as Clojure strings, with `"..."`, and those must be quoted for the shell correctly, with `'...'` around the string.

If the folder for `project-name` already exists, `clj-new` will not overwrite it unless you specify the `:force` option.

#### Example Usage

Here are some examples, generating projects from existing templates:

```bash
    clojure -X:new create :template luminus :name yourname/example.webapp \
      :output '"mywebapp"' :args '["+http-kit" "+h2" "+reagent" "+auth"]'
```

This creates a folder called `mywebapp` with a Luminus web application that will use `http-kit`, the H2 database, the Reagent ClojureScript library, and the Buddy library for authentication. The `-main` function is in `yourname.example.webapp.core`, which is in the  `mywebapp/src/clj/yourname/example/webapp/core.clj` file. Note that the [Luminus template](https://github.com/luminus-framework/luminus-template) produces a Leiningen-based project, not a CLI/`deps.edn` one, but you can also tell it to produce a Boot-based project (with `+boot`).

```
    clojure -X:new create :template re-frame :name yourname/spa \
      :output '"front-end"' :args '["+garden" "+10x" "+routes"]'
```

This creates a folder called `front-end` with a ClojureScript Single Page Application that uses Garden for CSS, `re-frame-10x` for debugging, and Secretary for routing. The entry point is in the `yourname.spa.core` namespace which is in the `front-end/src/cljs/yourname/spa/core.cljs` file. As with Luminus, the [`re-frame` template](https://github.com/day8/re-frame-template) produces a Leiningen-based project, not a CLI/`deps.edn` one.

```
    clojure -X:new create :template electron-app :name yourname/example
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

A minimal example, using the default bare bones template:

```
$ clojure -X:new create :template template :name myname/mytemplate
Generating a project called mytemplate that is a 'clj-new' template
```

You will now have a folder called `mytemplate` that is a very minimal template.

To create a new project based on that template, you need to have it on the classpath (just as if it were a library) and you also need `clj-new` on the classpath since you are using it to generate a project from that template:

```
$ clojure -Sdeps '{:deps {myname/mytemplate {:local/root "mytemplate"}}}' \
  -X:new create :template mytemplate :name myname/myproject
Generating fresh 'clj new' mytemplate project.
$ tree myproject
myproject
|____deps.edn
|____src
| |____myname
| | |____myproject
| | | |____foo.clj
```

This example uses a local template project structure, which is probably a good idea when you are developing your template, because the only real way to test a template is by trying to use it to generate a new project.

Once you have it working, you can publish it to GitHub or Clojars just like a regular library.

#### Arguments

Previous sections have revealed that it is possible to pass arguments to templates. For example:

```
    clojure -X:new create :template custom-template :name project-name \
      :args '[arg1 arg2 arg3]'
```

These arguments are accessible in the `custom-template` function as a second argument.

```clj
(ns clj.new.custom-template ,,,)

(defn custom-template
  [name & args]
  (println name " has the following arguments: " args))
```

Nearly all templates will expect these to be strings so you will need to quote them in the `:args` vector:

```
    clojure -X:new create :template custom-template :name project-name \
      :args '["arg1" "arg2" "arg3"]'
```

## clj Generators

Whereas clj templates will generate an entire new project in a new directory, clj generators are intended to add / modify code in an existing project. `clojure -X:new generate` will one or more generators, based on a `:generate` vector argument that you provide. Each generator in the vector is a string -- either `"type"` or `"type=name"`. The `type` specifies the type of generator to use. The `name` is the main argument that is passed to the generator.

A clj generator can be part of a project or a template. A generator `foo`, has a `clj.generate.foo/generate` function that accepts at least two arguments, `prefix` and the `name` specified as the main argument. `prefix` specifies the directory in which to perform the code generation and defaults to `src` (it cannot currently be overridden). In addition, any additional arguments are passed as additional arguments to the generator.

There are currently a few built-in generators:
- `file`
- `ns`
- `def`
- `defn`
- `edn`

The `file` generator creates files relative to the prefix. It optionally accepts a body, and file extension, supplied via an `:args` vector of strings. Those default to `nil` and `"clj"` respectively.
```bash
# Inside project folder, relying on the clj-new dependency.
clojure -X:new generate :generate '["file=foo.bar"]' :args '["(ns foo.bar)" "clj"]'
```

The `ns` generator creates a clojure namespace by using the `file` generator and providing a few defaults.
```bash
clojure -X:new generate :generate '["ns=foo.bar"]'
```

This will generate `src/foo/bar.clj` containing `(ns foo.bar)` (and a placeholder docstring). It will not replace an existing file.
```bash
clojure -X:new generate :generate '["defn=foo.bar/my-func"]'
```

If `src/foo/bar.clj` does not exist, it will be generated as a namespace first (using the `ns` generator above), then a definition for `my-func` will be appended to that file (with a placeholder docstring and a dummy argument vector of `[args]`). The generator does not check whether that `defn` already exists so it always appends a new `defn`.

Both the `def` and `defn` generators create files using the `ns` generator above.

The `edn` generator uses the `file` generator internally, with a default extension of `"edn"`.
```bash
clojure -X:new generate :generate '["edn=foo.bar"]' :args '["(ns foo.bar)"]'
```

You can provide as many generators as you want in the `:generate` vector, but if you provide an `:args` vector then those arguments will be passed into each of the generator functions, so you may still need to run multiple `clojure -X:new generate` commands.

The exec-args available for the `generate` function are:

* `:generate` -- a (non-empty) vector of generator strings to use
* `:args` -- an optional vector of string to pass to the generator itself as command-line arguments
* `:force` -- if `true`, will force overwrite the target directory/file if it exists
* `:help` -- if `true`, will provide a summary of these options as help
* `:prefix` -- specify the project directory in which to run the generator (the default is `src` but `:p '"."'` will allow a generator to modify files in the root of your project)
* `:snapshot` -- if `true`, look for -SNAPSHOT version of the template (not just a release version)
* `:template` -- load this template (using the same rules as for `clj-new/create` above) and then run the specified generator
* `:version` -- use this specific version of the template

# Releases

This project follows the version scheme MAJOR.MINOR.COMMITS where MAJOR and MINOR provide some relative indication of the size of the change, but do not follow semantic versioning. In general, all changes endeavor to be non-breaking (by moving to new names rather than by breaking existing names). COMMITS is an ever-increasing counter of commits since the beginning of this repository.

Latest stable release: 1.1.234

## Roadmap

* Improve the built-in template `template` so that it can be used to seed a new `clj` project.

## License

Copyright © 2016-2020 Sean Corfield and the Leiningen Team for much of the code -- thank you!

Distributed under the Eclipse Public License version 1.0.
