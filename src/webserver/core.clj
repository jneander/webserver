(ns webserver.core
  (:require [webserver.socket-connection 
             :refer [open-server-socket listen-and-respond]]
            [webserver.handler :refer [print-response]]))

(defn -main [& args]
  (let [port (if (empty? args) 8080 (read-string (first args)))
        server-socket (open-server-socket port)]
    (while (not (.isClosed server-socket))
      (listen-and-respond server-socket print-response))))
