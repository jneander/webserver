(ns webserver.handler-test
  (:require [webserver.handler :refer :all]
            [speclj.core :refer [describe it should= should]])
  (:import [java.io BufferedReader StringReader PrintStream OutputStream]))

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

(defn- string-contains? [pattern source]
  (= pattern (re-find (re-pattern pattern) source)))

(describe "#route-request"

  (it "sends directory requests to directory-response"
    (let [response (route-request (request "/sample_directory"))]
      (should= "<p>sample.txt</p>" (:body response))))
  
  (it "routes '/form' to ok-response"
    (let [response (route-request {:path "/form"})]
      (should= 200 (:status response)))))

(describe "#print-response"

  (it "includes header information"
    (let [output-tracker (track-request (sample-request-header "sample.txt") ".")
          output (last @output-tracker)]
      (should (string-contains? "HTTP/1.1 200 OK" output))
      (should (string-contains? "Host: localhost:8080" output))
      (should (string-contains? "Content-Type: text/html" output))
      (should (string-contains? "Content-Length: 7" output))))

  (it "merges the response header and body"
    (let [output-tracker (track-request (sample-request-header "sample.txt") ".")]
      (should= (sample-request-output) (last @output-tracker))))
  
  (it "uses the directory to serve as root"
    (let [request-header (str "GET /sample.txt HTTP/1.1\r\n"
                              "Host: localhost:8080\r\n\r\n")
          output-tracker (track-request request-header 
                                        "./spec/public_html/sample_directory")
          output (last @output-tracker)]
      (should (string-contains? "otherfoobar" output)))))
