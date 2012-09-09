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

(defn read-text-file [file]
  (let [body (slurp file)]
    {:length (.length body) :body body}))

(defn read-file [file]
  (slurp (.getCanonicalPath file)))

(defn- known-exts []
  {"txt" :text
   "jpeg" :binary})

(defn- known-content-types []
  {"txt" "text/plain"
   "jpeg" "image/jpeg"})

(defn- get-ext [file]
  (let [parts (clojure.string/split (.getName file) #"\.")]
    (if (> (.length parts) 1) (last parts) nil)))

(defn get-data-type [file]
  (let [ext (get-ext file)]
    (if ext (get (known-exts) ext) :unknown)))

(defn get-content-type [file]
  (let [ext (get-ext file)]
    (if ext (get (known-content-types) ext) "text/plain")))

(defn content-data-type [^String content-type]
  (if (= "image/jpeg" content-type)
    :binary
    :text))
