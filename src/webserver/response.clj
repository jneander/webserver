(ns webserver.response
  (:require [clojure.string :refer [join]])
  (:import [java.io File]))

(defn- status-codes []
  {200 "OK"
   404 "Not Found"})

(defn- list-directory [^File directory]
  (let [list (into [] (.list directory))]
    (clojure.string/join (map (fn [body] (str "<p>" body "</p>")) list))))

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
        query (clojure.string/split (:path request) #"\?")
        separated (clojure.string/split (last query) #"&")
        pairs (map (fn [x] (clojure.string/split x #"=")) separated)
        mapped (into {} pairs)
        formatted (map (fn [x] (str (key x) " = " (val x))) mapped)]
    (-> response
      (body (join "\r\n" formatted))
      (header request))))
