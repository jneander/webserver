(ns webserver.socket-connection-test
  (:use speclj.core
        (webserver socket-connection
                   request-handler))
  (:import (java.io BufferedReader OutputStream PrintStream)
           (java.net ServerSocket Socket)))

(defn reset-tracker[]
  (def tracker (atom [])))

(defn mock-client-streams []
  (def client-input-mock
    (proxy [java.io.StringBufferInputStream] ["test-input"]))
  (def client-output-mock
    (proxy [java.io.ByteArrayOutputStream] [])))

(defn echo [client-reader client-writer]
  (swap! tracker conj client-reader client-writer)
  (.println client-writer (.readLine client-reader)))

(defn mock-client-socket []
  (mock-client-streams)
  (def client-socket-mock
    (proxy [Socket] [] 
      (getOutputStream [] client-output-mock)
      (getInputStream [] client-input-mock))))

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
                  (mock-client-socket)
                  (def client-socket (connect-client-socket server-socket)))
          (after (.close server-socket)
                 (.close client-socket))
          (it "connects the client and server sockets"
              (should= client-socket-mock client-socket)))

(describe "#open-client-writer"
          (before (mock-client-streams)
                  (def output-stream (open-client-writer client-socket-mock)))
          (after (.close output-stream))
          (it "returns a PrintStream instance"
              (should= PrintStream (class output-stream)))
          (it "links the PrintStream to the client socket"
              (.print output-stream "test-output")
              (should= "test-output" (.toString client-output-mock))))

(describe "#open-client-reader"
          (before (mock-client-streams)
                  (def input-stream (open-client-reader client-socket-mock)))
          (after (.close input-stream))
          (it "returns an BufferedReader instance"
              (should= BufferedReader (class input-stream)))
          (it "links the BufferedReader to the client socket"
              (should= "test-input" (.readLine input-stream))))

(describe "#listen-and-respond"
          (before (reset-tracker)
                  (mock-client-streams)
                  (mock-client-socket)
                  (def server-socket (mock-server-socket 8080))
                  (listen-and-respond server-socket echo))
          (after (.close server-socket))
          (it "connects with and closes client socket"
              (should (.isClosed client-socket-mock)))
          (it "passes a client reader/writer to the service"
              (should= BufferedReader (class (first @tracker)))
              (should= PrintStream (class (second @tracker)))
              (should= "test-input\n" (.toString client-output-mock))))
