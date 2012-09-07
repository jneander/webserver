(ns webserver.socket-connection-test
  (:require [speclj.core :refer [describe it should= should before after]]
        [webserver.socket-connection :refer :all])
  (:import [java.io BufferedReader OutputStream PrintStream]
           [java.net ServerSocket Socket]))

(let [echo-tracker (atom [])]
  (defn- echo [client directory]
    (swap! echo-tracker conj client directory))
  (defn- get-echo [] @echo-tracker)
  (defn- reset-echo [] (reset! echo-tracker [])))

(let [client-input (proxy [java.io.StringBufferInputStream] ["test-input"])
      client-output (proxy [java.io.ByteArrayOutputStream] [])
      client (proxy [Socket] []
               (getInputStream [] client-input)
               (getOutputStream [] client-output))]
  (defn mock-server [port]
    (proxy [ServerSocket] [port]
      (accept [] client))))

(describe "#open-server-socket"
  
  (with-open [socket (open-server-socket 8080)]
    (it "returns a ServerSocket instance"
      (should= ServerSocket (class socket))))

  (with-open [socket (open-server-socket 8080)]
    (it "uses the provided port"
      (should= 8080 (.getLocalPort socket)))))

(describe "#connect-client-socket"

  (with-open [server (mock-server 8080)
              client (.accept server)]
    (it "connects the client and server sockets"
      (should= client (connect-client-socket server)))))

(describe "listen-and-respond"

  (with-open [server (mock-server 8080)
              client (.accept server)]
    (listen-and-respond server echo ".")
    (Thread/sleep 1)

    (it "passes the client and directory to the service"
      (should= client (first (get-echo)))
      (should= "." (second (get-echo)))))

  (with-open [server (mock-server 8080)
              client (.accept server)]
    (listen-and-respond server echo ".")
    (Thread/sleep 1)

    (it "connects with and closes the client socket"
      (should (.isClosed client)))))
