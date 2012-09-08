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

(defn read-binary-file [file]
  (let [stream (java.io.FileInputStream. file)
        length (.length file)
        buffer (byte-array (map byte (take length (repeat 0))))]
    (.read stream buffer)
    {:length length :body buffer}))

(defn read-file [file]
  (slurp (.getCanonicalPath file)))

(defn- known-exts []
  {"txt" :text
   "jpeg" :binary})

(defn get-data-type [file]
  (let [parts (clojure.string/split (.getName file) #"\.")]
    (if (> (.length parts) 1)
      (get (known-exts) (last parts))
      :unknown)))
