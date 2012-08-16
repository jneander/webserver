(ns webserver.core)

(defn -main
  "I don't do a whole lot."
  [& args]
  (println "Hello, World!"))

(defn open-server-socket [port]
  (java.net.ServerSocket. port))
