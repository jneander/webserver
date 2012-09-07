(ns webserver.io
  (:import [java.io File]))

(defn read-file [file]
  (slurp (.getCanonicalPath file)))
