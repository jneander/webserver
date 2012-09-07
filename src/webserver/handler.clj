(ns webserver.handler
  (:require [webserver.request :refer [map-request]]
            [webserver.response :refer [resource-response
                                        echo-response
                                        echo-query-response]]
            [clojure.string :refer [join split]])
  (:import [java.io File]))

(defn- line-ending [] "\r\n")

(defn- get-route [request]
  (first (split (:path request) #"\?")))

(defmulti route-request get-route)

(defmethod route-request "/form" [request]
  (echo-response request))

(defmethod route-request "/some-script-url" [request]
  (echo-query-response request))

(defmethod route-request :default [request]
  (resource-response request))

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

(defn open-string-reader [client]
  (java.io.BufferedReader. 
    (java.io.InputStreamReader.
      (.getInputStream client))))

(defn open-string-writer [client]
  (java.io.PrintStream.
    (.getOutputStream client)))

(defn- parse-request [client]
  (let [input (open-string-reader client)]
    (map-request (read-request-header input))))

(defn route-response [client directory]
  (let [request (assoc (parse-request client) :directory directory)
        response (route-request request)
        statement (str (flatten-header response)
                       (line-ending)
                       (:body response))]
    (with-open [output (open-string-writer client)]
      (.println output statement))))
