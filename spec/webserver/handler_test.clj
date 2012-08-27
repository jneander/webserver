(ns webserver.handler-test
  (:require [webserver.handler :refer :all]
            [speclj.core :refer [describe it should= should]])
  (:import [java.io BufferedReader StringReader PrintStream OutputStream]))

(defn- request [path]
  {:type "GET" :path (str "/spec/public_html" path) :http "1.1"})

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

(defn- track-request [request]
  (let [output-tracker (tracker)
        client-reader (mock-client-reader request)
        client-writer (mock-client-writer output-tracker)]
    (print-response client-reader client-writer)
    output-tracker))

(defn- string-contains? [pattern source]
  (= pattern (re-find (re-pattern pattern) source)))

(describe "#route-request"
  (it "sends directory requests to directory-response"
    (let [response (route-request (request "/sample_directory"))]
      (should= "<p>file3.txt</p>" (:body response)))))

(describe "#print-response"
  (let [output-tracker (track-request (sample-request-header "sample.txt"))
        output (last @output-tracker)]
    (it "includes the status code"
      (should (string-contains? "HTTP/1.1 200 OK" output)))
    (it "includes the host"
      (should (string-contains? "Host: localhost:8080" output)))
    (it "includes the content type"
      (should (string-contains? "Content-Type: text/html" output)))
    (it "includes the content length"
      (should (string-contains? "Content-Length: 7" output))))

  (let [output-tracker (track-request (sample-request-header "sample.txt"))]
    (it "merges the response header and body"
      (should= (sample-request-output) (last @output-tracker)))))
