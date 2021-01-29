# {{name}}

{{description}}

## Usage

FIXME: write usage documentation!

Creating a project from this template (the `:new` alias in this template project defaults `:template` to {{name}}):

```bash
    clojure -X:new :name myname/myproject
    cd myproject
```

Build a deployable jar of this template:

    $ clojure -X:jar

This will update the generated `pom.xml` file to keep the dependencies synchronized with
your `deps.edn` file. You can update the version information in the `pom.xml` using the
`:version` argument:

    $ clojure -X:jar :version '"1.2.3"'

Install it locally (requires the `pom.xml` file):

    $ clojure -X:install

Deploy it to Clojars -- needs `CLOJARS_USERNAME` and `CLOJARS_PASSWORD` environment
variables (requires the `pom.xml` file):

    $ clojure -X:deploy

If you don't plan to install/deploy the template as a library, you can remove the
`pom.xml` file but you will also need to remove `:sync-pom true` from the `deps.edn`
file (in the `:exec-args` for `depstar`).

## License

Copyright © {{year}} {{developer}}

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
