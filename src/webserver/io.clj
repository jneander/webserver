(ns webserver.io
  (:import [java.io File]))

(defn open-string-reader [socket]
  (java.io.BufferedReader. 
    (java.io.InputStreamReader.
      (.getInputStream socket))))

(defn open-string-writer [socket]
  (java.io.PrintStream.
    (.getOutputStream socket)))

(defn open-binary-writer [socket]
  (java.io.FilterOutputStream.
    (.getOutputStream socket)))

(defn read-file [file]
  (slurp (.getCanonicalPath file)))
