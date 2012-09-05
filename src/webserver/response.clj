(ns webserver.response
  (:require [clojure.string :refer [join split]])
  (:import [java.io File]))

(defn- status-codes []
  {200 "OK"
   404 "Not Found"})

(defn to-list-item [item path] 
  (str "<p><a href=\"" path item "\">" item "</a></p>"))

(defn clean-path [directory]
  (let [path (last (re-find #"^[.]?(.*)/?" (.getPath directory)))]
    (str path (if-not (= "/" path) "/"))))

(defn- list-directory [^File directory]
  (let [list (into [] (.list directory))
        listify (fn [item] (to-list-item item (clean-path directory)))]
    (clojure.string/join (map listify list))))

(defn ok-response []
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

(defn- header [response request]
  (assoc response :header
         (merge (:header response)
                {:host (:host request)}
                {:content-type (content-type response)}
                {:content-length (content-length response)}
                {:status-message (status-message request response)})))

(defn- map-query-string [query]
  (let [query (last (split query #"\?"))
        sections (split query #"&")
        pairs (map (fn [x] (split x #"=")) sections)]
    (into {} pairs)))

(defn resource-response 
  [request-map directory]
  (let [file (File. directory (:path request-map))
        response (cond
                   (.isFile file)
                   (body (ok-response) (slurp (.getCanonicalPath file)))
                   (.isDirectory file)
                   (body (ok-response) (list-directory file))
                   :else (not-found))]
    (header response request-map)))

(defn echo-response [request-map directory]
  (-> (ok-response)
    (body (str (:host request-map) (:path request-map)))
    (header request-map)))

(defn echo-query-response [request directory]
  (let [response (ok-response)
        mapped-query (map-query-string (:path request))
        formatter (fn [x] (str (key x) " = " (val x)))
        formatted (map formatter mapped-query)]
    (-> response
      (body (join "\r\n" formatted))
      (header request))))
