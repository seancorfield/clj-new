# {{name}}

{{description}}

## Usage

FIXME: write usage documentation!

Invoke a library API function from the command-line:

    $ clojure -X {{namespace}}/foo :a 1 :b '"two"'
    {:a 1, :b "two"} "Hello, World!"

Run the project's tests (they'll fail until you edit them):

    $ clojure -M:test:runner

Build a deployable jar of this library:

    $ clojure -X:jar

Install it locally:

    $ clojure -M:install

Deploy it to Clojars -- needs `CLOJARS_USERNAME` and `CLOJARS_PASSWORD` environment variables:

    $ clojure -M:deploy

## License

Copyright © {{year}} {{developer}}

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
