(ns webserver.response
  (:import [java.io File]
           [java.lang String]))

(defn- ok-response []
  {:status 200
   :headers {}
   :body ""})

(defn not-found []
  {:status 404
   :headers {}
   :body ""})

(defn- body [response content]
  (assoc response :body content))

(defn file-response [^String filepath & [^String root]]
  (let [file (File. root filepath)]
    (if (.exists file)
      (body (ok-response) (slurp (.getCanonicalPath file)))
      (not-found))))
