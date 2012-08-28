(ns webserver.response
  (:import [java.io File]
           [java.lang String]))

(defn- status-codes []
  {200 "OK"
   404 "Not Found"})

(defn- list-directory [^File directory]
  (let [list (into [] (.list directory))]
    (clojure.string/join (map (fn [body] (str "<p>" body "</p>")) list))))

(defn- ok-response []
  {:status 200
   :header {}
   :body ""})

(defn not-found []
  {:status 404
   :header {}
   :body ""})

(defn- status-message [request response]
  (str "HTTP/" (:http request) 
       " " (:status response)
       " " (get (status-codes) (:status response))))

(defn- content-length [response]
  (.length (:body response)))

(defn- content-type [response]
  "text/html")

(defn- body [response content]
  (assoc response :body content))

(defn- header [request response]
  (assoc response :header
         (merge (:header response)
                {:host (:host request)}
                {:content-type (content-type response)}
                {:content-length (content-length response)}
                {:status-message (status-message request response)})))

(defn resource-response 
  ([request-map] (resource-response request-map "."))
  ([request-map directory]
   (let [file (File. directory (:path request-map))
         response (cond
                    (.isFile file)
                      (body (ok-response) (slurp (.getCanonicalPath file)))
                    (.isDirectory file)
                      (body (ok-response) (list-directory file))
                    :else (not-found))]
     (header request-map response))))
