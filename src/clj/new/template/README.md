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

Install it locally:

    $ clojure -X:install

Deploy it to Clojars -- needs `CLOJARS_USERNAME` and `CLOJARS_PASSWORD` environment variables:

    $ clojure -X:deploy

## License

Copyright © {{year}} {{developer}}

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
