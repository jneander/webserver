(ns webserver.socket-connection
  (:import (java.io BufferedReader
                    InputStreamReader
                    PrintStream)
           (java.net ServerSocket)))

(defn open-server-socket [port]
  (ServerSocket. port 150))

(defn connect-client-socket [server-socket]
  (.accept server-socket))

(defn- run-client-service [client service directory]
  (service client directory)
  (.close client))

(defn listen-and-respond [server service directory]
  (let [client (connect-client-socket server)]
    (.start (Thread. #(run-client-service client service directory)))))
