(ns webserver.spec-helper
  (:require [speclj.core :refer [should= should]]))

(defn should-contain [match target]
  (should= match (or (re-find (re-pattern match) target)
                     target)))
