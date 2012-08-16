(ns webserver.core-test
  (:use speclj.core
        webserver.core))

(import '(java.net ServerSocket Socket)
        '(java.io OutputStream PrintStream))

(def client-output-mock (proxy [java.io.ByteArrayOutputStream] []))
(def client-socket-mock 
  (proxy [Socket] [] (getOutputStream [] client-output-mock)))

(defn mock-server-socket [port]
  (proxy [ServerSocket] [port]
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

(describe "#open-client-writer"
          (before (def output-stream (open-client-writer client-socket-mock)))
          (after (.close output-stream))
          (it "returns a PrintStream instance"
              (should= PrintStream (class output-stream)))
          (it "links the PrintStream to the client socket"
              (.print output-stream "test-value")
              (should= "test-value" (.toString client-output-mock))))
