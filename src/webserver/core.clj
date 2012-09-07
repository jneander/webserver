(ns webserver.core
  (:require [webserver.socket-connection 
             :refer [open-server-socket listen-and-respond]]
            [webserver.handler :refer [route-response]])
  (:gen-class :main true))

(defn parse-arguments [args]
  (let [mapped (apply hash-map args)]
    {:port (if (contains? mapped "-p") (read-string (get mapped "-p")) 8080)
     :directory (if (contains? mapped "-d") (get mapped "-d") ".")}))

(defn -main [& args]
  (let [arg-map (parse-arguments args)
        server-socket (open-server-socket (:port arg-map))]
    (while (not (.isClosed server-socket))
      (listen-and-respond server-socket route-response (:directory arg-map)))))
