(ns leiningen.release
  "Adapted from Leiningen's code by stripping things down to
  just one method to support one particular template (play-clj)
  that uses lein-droid, which expects parse-semantic-version
  to be available here.")

(defn string->semantic-version
  "Create map representing the given version string. Returns nil if the
  string does not follow guidelines setforth by Semantic Versioning 2.0.0,
  http://semver.org/"
  [version-string]
  ;; <MajorVersion>.<MinorVersion>.<PatchVersion>[-<Qualifier>][-SNAPSHOT]
  (if-let [[_ major minor patch qualifier snapshot]
           (re-matches
            #"(\d+)\.(\d+)\.(\d+)(?:-(?!SNAPSHOT)([^\-]+))?(?:-(SNAPSHOT))?"
            version-string)]
    (->> [major minor patch]
         (map #(Integer/parseInt %))
         (zipmap [:major :minor :patch])
         (merge {:qualifier qualifier
                 :snapshot snapshot}))))

(defn parse-semantic-version
  "Create map representing the given version string. Aborts with exit code 1
  if the string does not follow guidelines setforth by Semantic Versioning 2.0.0,
  http://semver.org/"
  [version-string]
  (or (string->semantic-version version-string)
      (throw (ex-info (format "Unrecognized version string: %s" version-string)
                      {}))))
