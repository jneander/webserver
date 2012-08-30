(ns webserver.socket-connection
  (:import (java.io BufferedReader
                    InputStreamReader
                    PrintStream)
           (java.net ServerSocket)))

(defn open-server-socket [port]
  (ServerSocket. port 150))

(defn connect-client-socket [server-socket]
  (.accept server-socket))

(defn open-client-writer [client-socket]
  (PrintStream. (.getOutputStream client-socket)))

(defn open-client-reader [client-socket]
  (BufferedReader. 
    (InputStreamReader. 
      (.getInputStream client-socket))))

(defn- run-client-service [client-socket service directory]
  (with-open [client-reader (open-client-reader client-socket)
              client-writer (open-client-writer client-socket)]
    (service client-reader client-writer directory)
    (.close client-socket)))

(defn listen-and-respond [server-socket service directory]
  (let [client-socket (connect-client-socket server-socket)]
    (.start (Thread. #(run-client-service client-socket service directory)))))
