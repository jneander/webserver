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

(defn- stub-request-header [path]
  (str "GET " path " HTTP/1.1\r\n"
       "Host: localhost:8080\r\n\r\n"))

(defn- stub-response-map []
  {:status 200
   :header {:status-message "HTTP/1.1 200 OK"
            :host "localhost:8080"
            :content-type "text/html"
            :content-length 7}
   :body "foobar"})

(defn- stub-request-output []
  (str "HTTP/1.1 200 OK\r\n"
       "Host: localhost:8080\r\n"
       "Content-Type: text/plain\r\n"
       "Content-Length: 7\r\n"
       "\r\n"
       "foobar\n"))

(defn- route-response-output [path directory]
  (let [request (stub-request-header path)
        client (mock-client-request request)]
    (route-response client directory)))

(describe "#route-response"

  (it "includes header information"
    (reset-client-output)
    (route-response-output "/spec/public_html/sample.txt" ".")
    (let [output (.toString (.getOutputStream (mock-client-socket)))]
      (should-contain "HTTP/1.1 200 OK" output)
      (should-contain "Host: localhost:8080" output)
      (should-contain "Content-Type: text/plain" output)
      (should-contain "Content-Length: 7" output)))

  (it "merges the response header and body"
    (reset-client-output)
    (route-response-output "/spec/public_html/sample.txt" ".")
    (let [output (.toString (.getOutputStream (mock-client-socket)))]
      (should-contain (stub-request-output) output)))

  (it "uses the directory to serve as root"
    (reset-client-output)
    (route-response-output "/sample.txt" "./spec/public_html/sample_directory")
    (let [output (.toString (.getOutputStream (mock-client-socket)))]
      (should-contain "otherfoobar" output)))
  
  (it "uses a binary writer for a binary response body"
    (reset-client-output)
    (route-response-output "/spec/public_html/image.jpeg" ".")
    (should= 38490 (.size (.getOutputStream (mock-client-socket)))))

  (it "includes 'Location' when routing redirect"
    (reset-client-output)
    (route-response-output "/redirect" ".")
    (let [output (.toString (.getOutputStream (mock-client-socket)))]
      (should-contain "Location: /" output))))
