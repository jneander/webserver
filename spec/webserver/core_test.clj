(ns webserver.core-test
  (:use speclj.core
        webserver.core)
  (:import (java.io BufferedReader)))

(import '(java.net ServerSocket Socket)
        '(java.io OutputStream PrintStream StringReader))

(def short-header (str "one\ntwo\n\nthree\n"))
(def full-header (str
  "GET / HTTP/1.1\n"
  "Host: localhost:8080\n"
  "Connection: keep-alive\n"
  "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) "
  "AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.75 Safari/537.1\n"
  "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\n"
  "Accept-Encoding: gzip,deflate,sdch\n"
  "Accept-Language: en-US,en;q=0.8\n"
  "Accept-Charset: ISO-8859-1,utf-8;q=0.7\n\n"))

(def client-output-mock (proxy [java.io.ByteArrayOutputStream] []))
(def client-input-mock (proxy [java.io.StringBufferInputStream] ["test-string"]))
(def client-socket-mock 
  (proxy [Socket] [] (getOutputStream [] client-output-mock)
    (getInputStream [] client-input-mock)))

(defn mock-server-socket [port]
  (proxy [ServerSocket] [port]
    (accept [] client-socket-mock)))
(defn mock-client-reader [string]
  (BufferedReader. (StringReader. string)))

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

(describe "#open-client-reader"
          (before (def input-stream (open-client-reader client-socket-mock)))
          (after (.close input-stream))
          (it "returns an BufferedReader instance"
              (should= BufferedReader (class input-stream)))
          (it "links the BufferedReader to the client socket"
              (should= "test-string" (.readLine input-stream))))

(describe "#parse-get-request"
          (it "matches to a slash"
            (should= "/" (parse-get-request "GET / HTTP/1.1")))
          (it "does not match"
            (should= nil (parse-get-request "FOO GET / HTTP")))
          (it "matches to longer request"
            (should= "/foo/bar/" (parse-get-request "GET /foo/bar/ HTTP"))))

(describe "#get-header-lines"
          (before (def header-lines (get-header-lines (mock-client-reader short-header))))
          (it "returns a vector of strings"
              (should= java.lang.String (class (first header-lines))))
          (it "reads up to empty line"
              (should= ["one" "two"] header-lines))
          (it "stops at empty line"
              (should-not (some #{"three"} header-lines))))

(describe "#get-host"
          (before (def header-lines (get-header-lines (mock-client-reader full-header)))
                  (def failing-lines (get-header-lines (mock-client-reader short-header))))
          (it "returns 'Host' value when found"
              (should= "localhost:8080" (get-host header-lines)))
          (it "returns nil otherwise"
              (should= nil (get-host failing-lines))))
