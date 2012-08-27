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

(defn- content-length [response]
  (.length (:body response)))

(defn- body [response content]
  (assoc response :body content))

(defn- header [response]
  (assoc response :header 
         (merge (:header response) 
                {:content-length (content-length response)})))

(defn resource-response [request-map]
  (let [file (File. "." (:path request-map))
        response (cond
                   (.isFile file)
                     (body (ok-response) (slurp (.getCanonicalPath file)))
                   (.isDirectory file)
                     (body (ok-response) (list-directory file))
                   :else (not-found))]
    (header response)))
