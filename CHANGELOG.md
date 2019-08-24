# Changes

* 0.7.8 -- 2019-08-24
  * Fixes `-v` / `--verbose` option handling (again!).
  * Updates `org.clojure/test.check` to `"0.10.0"` and `tools.deps.alpha` to 0.7.541 (and add `slf4j-nop` as a dependency now that t.d.a has removed it).
  * Pins Jackson libraries in `deps.edn` to avoid potential version conflicts (such as when generating a Luminus template).
* 0.7.7 -- 2019-08-07
  * Fixed #23 by pinning versions in the templates (`org.clojure/clojure "1.10.1"` and `org.clojure/test.check "0.10.0-RC1"`).
  * Also updates `tools.deps.alpha` to 0.7.527.
* 0.7.6 -- 2019-07-04
  * Fixes #20 by allowing more complex Git URLs (and documenting them in the README).
  * Fixes #15 by allowing (and ignoring) `nil` paths to `->files`.
* 0.7.5 -- 2019-06-29
  * Fixes #21 by updating `tools.deps.alpha` (to 0.7.516) and switching from `clojure-env` to `default-deps`.
  * Fixes #19 by expanding the explanation of qualified/dotted project names in the README.
  * Fixes #18 by supporting dotted names in templates.
  * Fixes #14 by adding `root-ns` to the `template` setup.
  * Fixes `-v` / `--verbose` option handling.
* 0.5.5 -- 2018-11-12
  * Update `tools.deps.alpha` version.
* 0.5.4 -- 2018-10-24
  * Initial version that "matches" `tools.deps.alpha` versioning.
