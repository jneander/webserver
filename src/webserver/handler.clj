(ns webserver.handler
  (:require [webserver.request :refer [map-request]]
            [webserver.router :refer [route-request]]
            [webserver.io :refer [open-string-reader
                                  open-string-writer]]
            [clojure.string :refer [join split]])
  (:import [java.io File]))

(defn- line-ending [] "\r\n")

(defn- read-request-header [client-reader]
  (while (not (.ready client-reader)) (Thread/sleep 1))
  (loop [header "" line (.readLine client-reader)]
    (if (and line (< 0 (.length line)))
      (recur (str header line (line-ending)) (.readLine client-reader))
      header)))

(defn- flatten-header [response]
  (let [header-map (:header response)]
    (join (line-ending) 
        [(:status-message header-map)
         (str "Host: " (:host header-map))
         (str "Content-Type: " (:content-type header-map))
         (str "Content-Length: " (:content-length header-map) (line-ending))])))

(defn- flatten-response [response]
  {:body-type :text
   :header (flatten-header response)
   :body (:body response)})

(defn- parse-request [client]
  (let [input (open-string-reader client)]
    (map-request (read-request-header input))))

(defn- get-data-type [socket {type :body-type}] type)

(defmulti submit-response get-data-type)

(defmethod submit-response :default [socket response]
  (let [output (open-string-writer socket)]
    (.println output (str (:header response)
                          (line-ending)
                          (:body response)))))

(defn route-response [client directory]
  (let [request (assoc (parse-request client) :directory directory)
        response (flatten-response (route-request request))]
    (submit-response client response)))
