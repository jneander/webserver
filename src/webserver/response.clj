(ns webserver.response
  (:require [webserver.io :refer [read-text-file
                                  read-binary-file
                                  get-data-type
                                  get-content-type]]
            [clojure.string :refer [join split]])
  (:import [java.io File]))

(defn- status-codes []
  {200 "OK"
   404 "Not Found"})

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

(defn- assoc-header [response key value]
  (assoc response :header (assoc (:header response) key value)))

(defn- header [response request]
  (-> response
    (assoc-header :host (:host request))
    (assoc-header :status-message (status-message request response))))

(defn- content-type [response type]
  (assoc-header response :content-type type))

(defn- content-length [response & [length]]
  (assoc-header response :content-length 
                (or length
                    (.length (:body response)))))

(defn- body [response content]
  (assoc response :body content))

(defn- map-query-string [query]
  (let [query (last (split query #"\?"))
        sections (split query #"&")
        pairs (map (fn [x] (split x #"=")) sections)]
    (into {} pairs)))

(defn- read-file [file]
  (if-not (= :binary (get-data-type file))
    (read-text-file file)
    (read-binary-file file)))

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

(defn- file-into-response [file response]
  (let [file-result (read-file file)]
    (-> response
      (body (:body file-result))
      (content-type (get-content-type file))
      (content-length (:length file-result)))))

(defn- directory-into-response [request response]
  (-> response
    (body (list-directory request))
    (content-type "text/html")
    (content-length)))

(defn resource-response [request-map]
  (let [file (File. (:directory request-map) (:path request-map))
        response (cond
                   (.isFile file)
                   (file-into-response file (ok-response))
                   (.isDirectory file)
                   (directory-into-response request-map (ok-response))
                   :else (not-found))]
    (header response request-map)))

(defn echo-response [request-map]
  (-> (ok-response)
    (body (str (:host request-map) (:path request-map)))
    (content-type "text/plain")
    (content-length)
    (header request-map)))

(defn echo-query-response [request]
  (let [response (ok-response)
        mapped-query (map-query-string (:path request))
        formatter (fn [x] (str (key x) " = " (val x)))
        formatted (map formatter mapped-query)]
    (-> response
      (body (join "\r\n" formatted))
      (content-type "text/plain")
      (content-length)
      (header request))))
