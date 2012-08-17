(ns webserver.core
  (:import (java.io BufferedReader
                    InputStreamReader)))

(import java.io.PrintStream java.net.ServerSocket)

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

(defn parse-get-request [string]
  ((fn [string] (if (not (nil? string)) (last string)))
     (re-find #"^GET (.*) HTTP" string)))

(defn get-header-lines [client-reader]
  (loop [prev nil line (.readLine client-reader) lines []] 
    (if (< 0 (.length line)) 
      (recur line (.readLine client-reader) (conj lines line))
      lines)))
