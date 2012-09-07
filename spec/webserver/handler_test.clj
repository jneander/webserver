(ns webserver.handler-test
  (:require [webserver.handler :refer :all]
            [webserver.spec-helper :refer [should-contain]]
            [speclj.core :refer [describe it should= should around]])
  (:import [java.io BufferedReader StringReader PrintStream OutputStream]
           [java.net Socket]))

(let [client-input-stream (proxy [java.io.StringBufferInputStream] ["test-input"])
      client-output-stream (proxy [java.io.ByteArrayOutputStream] [])]
  (defn- mock-client-socket []
    (proxy [Socket] []
      (getInputStream [] client-input-stream)
      (getOutputStream [] client-output-stream)))
  (defn- mock-client-request [request]
    (proxy [Socket] []
      (getInputStream [] (proxy [java.io.StringBufferInputStream] [request]))
      (getOutputStream [] client-output-stream)))
  (defn- reset-client-output []
    (.reset client-output-stream)))

(defn- request [path]
  {:type "GET" :path (str "/spec/public_html" path) 
   :http "1.1" :directory "."})

(defn- sample-request-header [path]
  (str "GET " path " HTTP/1.1\r\n"
       "Host: localhost:8080\r\n\r\n"))

(defn- sample-request-output []
  (str "HTTP/1.1 200 OK\r\n"
       "Host: localhost:8080\r\n"
       "Content-Type: text/html\r\n"
       "Content-Length: 7\r\n"
       "\r\n"
       "foobar\n"))

(defn- route-response-output [path directory]
  (let [request (sample-request-header path)
        client (mock-client-request request)
        __ (route-response client directory)]
    (.toString (.getOutputStream (mock-client-socket)))))

(describe "#route-request"

  (it "sends directory requests to directory-response"
    (let [response (route-request (request "/sample_directory"))]
      (should= (str "<p><a href=\"/spec/public_html/sample_directory/"
                    "sample.txt\">sample.txt</a></p>")
               (:body response))))

  (it "routes '/form' to ok-response"
    (let [response (route-request {:path "/form"})]
      (should= 200 (:status response)))))

(describe "#route-response"

  (it "includes header information"
    (let [output (route-response-output "/spec/public_html/sample.txt" ".")]
      (should-contain "HTTP/1.1 200 OK" output)
      (should-contain "Host: localhost:8080" output)
      (should-contain "Content-Type: text/html" output)
      (should-contain "Content-Length: 7" output)))

  (it "merges the response header and body"
    (let [output (route-response-output "/spec/public_html/sample.txt" ".")]
      (should-contain (sample-request-output) output)))

  (it "uses the directory to serve as root"
    (let [output (route-response-output "/sample.txt"
                                        "./spec/public_html/sample_directory")]
      (should-contain "otherfoobar" output))))

(describe "#open-string-reader"

  (it "returns a BufferedReader connected to the client"
    (with-open [client (mock-client-socket)
                reader (open-string-reader client)]
      (should= BufferedReader (class reader))
      (should= "test-input" (.readLine reader)))))

(describe "#open-string-writer"

  (it "returns a PrintStream connected to the client"
    (with-open [client (mock-client-socket)
                writer (open-string-writer client)]
      (reset-client-output)
      (should= PrintStream (class writer))
      (.print writer "test-output")
      (should= "test-output" (.toString (.getOutputStream client))))))
