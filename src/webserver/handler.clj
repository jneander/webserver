(ns webserver.handler
  (:require [webserver.request :refer [map-request]]
            [webserver.response :refer [resource-response
                                        ok-response]]
            [clojure.string :refer [join]])
  (:import [java.io File]))

(defn- line-ending [] "\r\n")

(defn- server-directory []
  (.getCanonicalPath (File. ".")))

(defmulti route-request :path)

(defmethod route-request :default [request]
  (resource-response request))

(defn- read-request-header [client-reader]
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

(defn print-response [client-reader client-writer directory]
  (let [request (map-request (read-request-header client-reader))
        response (resource-response request directory)]
    (.println client-writer
              (str (flatten-header response)
                   (line-ending)
                   (:body response)))))
