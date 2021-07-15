# Changes

* 1.1.317 -- 2021-07-15
  * Fix #66 by tweaking filenames to avoid problems with cljdoc.org.
  * Update `depstar` and `tools.deps.alpha`.

* 1.1.314 -- 2021-07-09
  * Add `:tools/usage` and instructions for new CLI `tools` support.
  * Address #63 by combining `:test` and `:runner` now we're using `-X`.

* 1.1.309 -- 2021-05-18
  * Update `test-runner` and switch to `-X` invocation in templates.
  * Address #55 by modernizing the help feature and trying to deduce the correct usage from whatever aliases you have.
  * Clarify that the `polylith` template must be used with `clojure -M:poly` instead of the native `poly` command, and update SHA to latest `issue-66` branch head.

* 1.1.297 -- 2021-04-09
  * Treat `:output` and `:args` as strings (i.e., they will accept symbols and convert them to strings, the same way `:name` and `:template` were already handled); adds `:edn-args` as a way to pass pure EDN arguments into templates (this takes precedence over `:args` if both are present).
  * Update `:poly` tool SHA in generated projects.

* 1.1.293 -- 2021-04-02
  * Add a new built-in template type: `polylith`.
  * Update `.gitignore`/`.hgignore` files to ignore Calva's output window file and `.lsp/sqlite.db` (but allow `.lsp/config.edn` to remain under version control).
  * Update default versions of Clojure, `depstar`, `tools.deps.alpha`.

* 1.1.264 -- 2021-03-03
  * Change coordinates to `com.github.seancorfield/clj-new` (although new versions will continue to be deployed to `seancorfield/clj-new` for a while -- see the [Clojars Verified Group Names policy](https://github.com/clojars/clojars-web/wiki/Verified-Group-Names)).
  * Fix #54 by adopting a group/artifact naming convention for templates that complies with the Clojars VGN policy and inserts `clj-template.` into the artifact name.
  * Fix #52 by setting initial SCM tag to `v{{version}}` in `pom.xml` files.
  * Fix #51 by adjusting the initial group ID, artifact ID, SCM domain name and username; uses a number of heuristics to try to derive those from the supplied project name in a way that should encourage users to adopt project names that are reverse-domain-name/project-name; in addition `:scm-user` is available as an "env" variable that can override the derived value.
  * Update `tools.deps.alpha` to 0.9.884.
  * Update `depstar` to 2.0.193 (and switching to `com.github.seancorfield/depstar`) in templates.

* 1.1.243 -- 2021-01-29
  * Expand `README.md` (and generated `README.md` files) to clarify the presence and use of the `pom.xml` file -- and make it clear that you can remove it if you don't need/want it (and how to update the `deps.edn` file if you do remove it). Addresses part of #50.
  * Expand `README.md` (and generated `README.md` files) to clarify the presence and use of the `LICENSE` file -- and make it clear that you can release your projects under whatever license you want, or not release them as open source at all if you don't want to. Addresses #49.
  * Expand `README.md`'s discussion about qualified project names to provide more justification. Fixes #48.
  * Update default version of Clojure to 1.10.2 in templates.
  * Update `deps-deploy` to 0.1.5 and switch to `-X` invocation in templates. Fixes #47.
  * Update `depstar` to 2.0.171 in templates and auto-sync `pom.xml`. Fixes #50.

* 1.1.234 -- 2020-12-28
  * Improve examples of `-X` usage in `app` and `template` templates.
  * Update `test.check` to 1.1.0 in templates.
  * Update `tools.deps.alpha` to 0.9.857.
  * Update `depstar` to 2.0.165 in templates and switch to `-X` invocation.

* 1.1.228 -- 2020-10-09
  * Update `depstar` to 1.1.128 in templates (for license handling bug fix).

* 1.1.226 -- 2020-10-08
  * Fix #44 by adding `:scm-domain` as an "env" variable that can be substituted.
  * Update examples to reflect updated Clojure CLI (1.10.1.697 and later) with `-X` exec option.
  * Update `tools.deps.alpha` to 0.9.816.
  * Update `deps-deploy` to reflect new group ID and version.
  * Update `depstar` to 1.1.126 in templates.

* 1.1.216 -- 2020-09-07
  * Fix #43 by restoring support for older Clojure CLI versions (by falling back to explicitly finding & merging EDN files if no runtime basis is available).

* 1.1.215 -- 2020-09-06
  * Fix #41 by providing `clj-new/create` and `clj-new/generate` as entry points that can be used by the Clojure CLI `-X` option (to execute a specific function and pass a map of arguments).
  * Update to `depstar` 1.1.104 in templates.
  * Update to `tools.deps.alpha` 0.9.782 and use the runtime basis instead of trying to read the default `deps.edn` files.

* 1.0.211 -- 2020-07-29
  * Stop using single-segment lib names in templates.
  * Improve documentation of `-v`/`--verbose` option (#39).
  * Add test running instructions to generated readme for `lib` template.

* 1.0.199 -- 2020-04-10
  * Add documentation on how to find 3rd party templates (PR #37, @holyjak).
  * Update to `depstar` 1.0.94 and `test.check` 1.0.0 in templates.
  * Move to MAJOR.MINOR.COMMITS versioning scheme.
* 0.9.0 -- 2020-02-13
  * Remove Jackson dependencies since `tools.deps.alpha` no longer brings in a version (after the S3 Transporter change), so there's no longer a potential conflict with templates.
  * Various documentation improvements.
* 0.8.6 -- 2020-01-24
  * Attempt to fix #33 by bumping dependencies across the board. Note: we still pin Jackson to 2.7.5 to reduce transitive version conflicts in (Leiningen) templates.
* 0.8.5 -- 2020-01-17
  * Add `install`/`deploy` aliases to `lib`/`template` project generators.
* 0.8.4 -- 2020-01-02
  * Update to `depstar` 0.5.1 for bug fix to main namespaces containing `-`.
* 0.8.3 -- 2020-01-02
  * Update to `depstar` 0.5.0 and remove `classes` folder since `depstar` manages that automatically now.
* 0.8.2 -- 2019-12-31
  * Addresses #30 by updating `depstar` to 0.4.1 and relying on its `-C` option for AOT in `app`'s `:uberjar` alias.
  * Fixes #29 by changing group/artifact in `template` project.
  * Ensure `.keep` is a file, not a directory.
* 0.8.1 -- 2019-12-29
  * Adds `pom.xml` generation to `app` built-in template.
  * Adds `:uberjar` alias to `app` built-in template and `:jar` alias to `lib` and `template` built-in templates.
  * Expand documentation for built-in templates, including environment variables used in `pom.xml` files.
* 0.8.0 -- 2019-12-25
  * Fixes #28 by adding `-?` / `--query` option to explain what `clj-new` will attempt to do.
  * Fixes #27 by adding `-e` / `--env` option to add "environment variables" that will be available to templates via the new `project-data` function; also standardizes the data passed to the `app`, `lib`, and `template` built-in templates.
  * Fixes #25 by adding `pom.xml` to `lib` and `template` built-in templates.
  * Fixes some issues with the `template` project generator.
  * Update `seancorfield/clj-new` coordinates in generated projects (to use current version).
  * Update Cognitect's `test-runner` to latest SHA in generated projects.
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
  * Fixes #14 by adding `root-ns` to the `template` setup (renamed to `namespace` in 0.8.0).
  * Fixes `-v` / `--verbose` option handling.
* 0.5.5 -- 2018-11-12
  * Update `tools.deps.alpha` version.
* 0.5.4 -- 2018-10-24
  * Initial version that "matches" `tools.deps.alpha` versioning.
