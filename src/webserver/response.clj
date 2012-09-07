(ns webserver.response
  (:require [webserver.io :refer [read-file]]
            [clojure.string :refer [join split]])
  (:import [java.io File]))

(defn- status-codes []
  {200 "OK"
   404 "Not Found"})

(defn- to-link [path name]
  (str "<a href=\"" path name "\">" name "</a>"))

(defn- proper-path [path]
  (let [clean (last (re-find #"^[.]*(.*\w)" path))]
    (str clean (if-not (= "/" clean) "/"))))

(defn- list-directory [request]
  (let [file (File. (:directory request) (:path request))
        list (into [] (.list file))
        path (proper-path (:path request))]
    (join (map #(str "<p>" (to-link path %) "</p>") list))))

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
  [request-map]
  (let [file (File. (:directory request-map) (:path request-map))
        response (cond
                   (.isFile file)
                   (body (ok-response) (read-file file))
                   (.isDirectory file)
                   (body (ok-response) (list-directory request-map))
                   :else (not-found))]
    (header response request-map)))

(defn echo-response [request-map]
  (-> (ok-response)
    (body (str (:host request-map) (:path request-map)))
    (header request-map)))

(defn echo-query-response [request]
  (let [response (ok-response)
        mapped-query (map-query-string (:path request))
        formatter (fn [x] (str (key x) " = " (val x)))
        formatted (map formatter mapped-query)]
    (-> response
      (body (join "\r\n" formatted))
      (header request))))
