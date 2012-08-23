(ns webserver.core
  (:use (webserver socket-connection
                   request-handler)))

(defn -main [& args]
  (let [port (if (empty? args) 8080 (read-string (first args)))
        server-socket (open-server-socket port)]
    (while (not (.isClosed server-socket))
      (listen-and-respond server-socket print-response))))
