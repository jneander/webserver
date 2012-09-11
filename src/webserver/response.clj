(ns webserver.response
  (:require [webserver.mime :refer [ext-to-mime-type
                                    ext-to-data-type]]
            [webserver.io :refer [read-file
                                  get-ext]]
            [clojure.string :refer [join split]])
  (:import [java.io File]))

(defn- status-codes []
  {200 "OK"
   404 "Not Found"
   302 "Found"})

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

(defn- data-type [response type]
  (assoc response :data-type type))

(defn- header [response request]
  (-> response
    (assoc-header :host (:host request))
    (assoc-header :status-message (status-message request response))))

(defn- location [response path]
  (assoc-header response :location path))

(defn- content-type [response type]
  (assoc-header response :content-type type))

(defn- content-length [response & [length]]
  (assoc-header response :content-length 
                (or length
                    (.length (:body response)))))

(defn- body [response content]
  (assoc response :body content))

(defn- get-parameter-section [path]
  (second (split path #"\?")))

(defn- map-parameter-values [param-strings]
  (map #(let [pair (split % #"=")]
          [(first pair) (or (second pair) true)]) param-strings))

(defn- map-query-string [query]
  (let [query (second (split query #"\?"))
        sections (if query (split query #"&"))
        pairs (if sections (map-parameter-values sections))]
    (if pairs (into {} pairs))))

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
      (data-type (ext-to-data-type (get-ext file)))
      (body (:body file-result))
      (content-type (ext-to-mime-type (get-ext file)))
      (content-length (:length file-result)))))

(defn- directory-into-response [request response]
  (-> response
    (data-type :text)
    (body (list-directory request))
    (content-type "text/html")
    (content-length)))

(defn resource-response [request]
  (let [file (File. (:directory request) (:path request))
        response (cond
                   (.isFile file)
                   (file-into-response file (ok-response))
                   (.isDirectory file)
                   (directory-into-response request (ok-response))
                   :else (not-found))]
    (header response request)))

(defn echo-response [request]
  (-> (ok-response)
    (data-type :text)
    (body (str (:host request) (:path request)))
    (content-type "text/plain")
    (content-length)
    (header request)))

(defn echo-query-response [request]
  (let [mapped-query (map-query-string (:path request))
        formatter (fn [x] (str (key x) " = " (val x)))
        formatted (map formatter mapped-query)]
    (-> (ok-response)
      (data-type :text)
      (body (join "\r\n" formatted))
      (content-type "text/plain")
      (content-length)
      (header request))))

(defn redirect-response [request path]
  (let [response {:status 302}]
    (-> response
      (header request)
      (location path)
      (body "")
      (data-type :text)
      (content-length)
      (content-type "text/html"))))
