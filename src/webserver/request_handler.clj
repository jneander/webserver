(ns webserver.request-handler
  (:require [webserver.request :refer [map-request]]
            [webserver.response :refer [resource-response]]
            [clojure.string :refer [join]]))

(defn- newline [] "\r\n")

(defn- read-request-header [client-reader]
  (loop [header "" line (.readLine client-reader)]
    (if (< 0 (.length line))
      (recur (str header line (newline)) (.readLine client-reader))
      header)))

(defn- flatten-header [response]
  (let [header-map (:header response)]
    (join (newline) 
        [(:status-message header-map)
         (str "Host: " (:host header-map))
         (str "Content-Type: " (:content-type header-map))
         (str "Content-Length: " (:content-length header-map) (newline))])))

(defn print-response [client-reader client-writer]
  (let [request (map-request (read-request-header client-reader))
        response (resource-response request)]
    (.println client-writer
              (str (flatten-header response)
                   (newline)
                   (:body response)))))
