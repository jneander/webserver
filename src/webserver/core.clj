(ns webserver.core
  (:use (webserver socket-connection
                   request-handler)))

<<<<<<< Updated upstream
(defn -main [& args]
=======
(defn -main [& port]
>>>>>>> Stashed changes
  (let [server-socket (open-server-socket 8080)]
    (while (not (.isClosed server-socket))
      (listen-and-respond server-socket respond-to-request))))
