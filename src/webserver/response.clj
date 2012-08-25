(ns webserver.response
  (:import [java.io File]
           [java.lang String]))

(defn- list-directory [^File directory]
  (let [list (into [] (.list directory))]
    (clojure.string/join (map (fn [body] (str "<p>" body "</p>")) list))))

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

(defn directory-response [^String path & [^String root]]
  (let [file (File. root path)]
    (if (.exists file)
      (body (ok-response) (list-directory file))
      (not-found))))
