;; copyright (c) 2018 Sean Corfield, all rights reserved

(ns clj-new.create
  (:require [boot.new-helpers :as helpers]))

(defn -main [template-name project-name & args]
  (if (and template-name project-name)
    (helpers/create {:template template-name
                     :name project-name
                     :args []})))
