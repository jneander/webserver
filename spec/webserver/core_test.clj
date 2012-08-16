(ns webserver.core-test
  (:use speclj.core
        webserver.core))

(import '(java.net ServerSocket Socket))

(def client-socket-mock (Socket.))

(defn mock-server-socket [port]
  (proxy [ServerSocket] [port]
    (ServerSocket [port])
    (accept [] client-socket-mock)))

(describe "#open-server-socket"
          (before (def socket (open-server-socket 8080)))
          (after (.close socket))
          (it "returns a ServerSocket instance"
              (should= (.getClass socket) ServerSocket))
          (it "uses the provided port"
              (should= (.getLocalPort socket) 8080)))

(describe "#connect-client-socket"
          (before (def server-socket (mock-server-socket 8080))
                  (def client-socket (connect-client-socket server-socket)))
          (after (.close server-socket)
                 (.close client-socket))
          (it "connects the client and server sockets"
              (should= client-socket-mock client-socket)))
