# {{name}}

{{description}}

## Installation

Download from https://{{scm-domain}}/{{group}}/{{artifact}}

## Usage

FIXME: explanation

Run the project directly, via `:main-opts` (`-m {{namespace}}`):

    $ clojure -M:run-m
    Hello, World!

Run the project, overriding the name to be greeted:

    $ clojure -M:run-m Via-Main
    Hello, Via-Main!

Run the project directly, via `:exec-fn`:

    $ clojure -X:run-x
    Hello, Clojure!

Run the project, overriding the name to be greeted:

    $ clojure -X:run-x :name '"Someone"'
    Hello, Someone!

Run the project's tests (they'll fail until you edit them):

    $ clojure -M:test:runner

Build an uberjar:

    $ clojure -M:uberjar

Run that uberjar:

    $ java -jar {{name}}.jar

## Options

FIXME: listing of options this app accepts.

## Examples

...

### Bugs

...

### Any Other Sections
### That You Think
### Might be Useful

## License

Copyright © {{year}} {{developer}}

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
