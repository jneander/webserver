(ns webserver.handler-test
  (:require [webserver.handler :refer :all]
            [webserver.spec-helper :refer [should-contain]]
            [speclj.core :refer [describe it should= should]])
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
  (str "GET /spec/public_html/" path " HTTP/1.1\r\n"
       "Host: localhost:8080\r\n\r\n"))

(defn- sample-request-output []
  (str "HTTP/1.1 200 OK\r\n"
       "Host: localhost:8080\r\n"
       "Content-Type: text/html\r\n"
       "Content-Length: 7\r\n"
       "\r\n"
       "foobar\n"))

(defn- mock-client-reader [string]
  (BufferedReader. (StringReader. string)))

(defn- mock-client-writer [tracker]
  (let [output-stream (proxy [OutputStream] [])]
    (proxy [PrintStream] [output-stream]
      (println [arg] (swap! tracker conj arg)))))

(defn- tracker []
  (atom []))

(defn- track-request [request & [directory]]
  (let [output-tracker (tracker)
        client-reader (mock-client-reader request)
        client-writer (mock-client-writer output-tracker)]
    (print-response client-reader client-writer directory)
    output-tracker))

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
  
  (let [request (sample-request-header "sample.txt")
        client (mock-client-request request)
        __ (route-response client ".")
        output (.toString (.getOutputStream (mock-client-socket)))]

    (it "includes header information"
      (route-response client ".")
      (should-contain "HTTP/1.1 200 OK" output)
      (should-contain "Host: localhost:8080" output)
      (should-contain "Content-Type: text/html" output)
      (should-contain "Content-Length: 7" output))

    (it "merges the response header and body"
      (should-contain (sample-request-output) output)))
  
  (let [request (str "GET /sample.txt HTTP/1.1\r\n"
                     "Host: localhost:8080\r\n\r\n")
        client (mock-client-request request)
        __ (route-response client "./spec/public_html/sample_directory")
        output (.toString (.getOutputStream (mock-client-socket)))]

    (it "uses the directory to serve as root"
      (should-contain "otherfoobar" output))))

(describe "#print-response"

  (it "includes header information"
    (let [output-tracker (track-request (sample-request-header "sample.txt") ".")
          output (last @output-tracker)]
      (should-contain "HTTP/1.1 200 OK" output)
      (should-contain "Host: localhost:8080" output)
      (should-contain "Content-Type: text/html" output)
      (should-contain "Content-Length: 7" output)))

  (it "merges the response header and body"
    (let [output-tracker (track-request (sample-request-header "sample.txt") ".")]
      (should= (sample-request-output) (last @output-tracker))))
  
  (it "uses the directory to serve as root"
    (let [request-header (str "GET /sample.txt HTTP/1.1\r\n"
                              "Host: localhost:8080\r\n\r\n")
          output-tracker (track-request request-header 
                                        "./spec/public_html/sample_directory")
          output (last @output-tracker)]
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
