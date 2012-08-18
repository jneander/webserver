(ns webserver.core
  (:import (java.io BufferedReader
                    InputStreamReader
                    PrintStream)
           (java.net ServerSocket))

(defn -main
  "I don't do a whole lot."
  [& args]
  (println "Hello, World!"))

(defn open-server-socket [port]
  (ServerSocket. port))

(defn connect-client-socket [server-socket]
  (.accept server-socket))

(defn open-client-writer [client-socket]
  (PrintStream. (.getOutputStream client-socket)))

(defn open-client-reader [client-socket]
  (BufferedReader. 
    (InputStreamReader. 
      (.getInputStream client-socket))))
