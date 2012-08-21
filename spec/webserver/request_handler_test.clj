(ns webserver.request-handler-test
  (:use speclj.core
        webserver.request-handler)
  (:import (java.io BufferedReader StringReader PrintStream OutputStream)))

(def short-header (str "one\ntwo\n\nthree\n"))
(def full-header (str
  "GET /foo/bar HTTP/1.1\n"
  "Host: localhost:8080\n"
  "Connection: keep-alive\n"
  "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_6_8) "
  "AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.75 Safari/537.1\n"
  "Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8\n"
  "Accept-Encoding: gzip,deflate,sdch\n"
  "Accept-Language: en-US,en;q=0.8\n"
  "Accept-Charset: ISO-8859-1,utf-8;q=0.7\n\n"))

(defn mock-client-writer [tracker]
  (let [output-stream (proxy [OutputStream] [])]
    (proxy [PrintStream] [output-stream]
      (println [arg] (swap! tracker conj arg)))))
(defn mock-client-reader [string]
  (BufferedReader. (StringReader. string)))
(defn generate-request []
  (def header-lines (get-header-lines (mock-client-reader full-header)))
  (def request-fields (map-request-fields header-lines)))
(defn generate-response []
  (def response-body (get-response-body request-fields))
  (def response-map (get-response-map header-lines response-body))
  (def response-header (get-response-header response-map)))
(defn string-contains? [pattern source]
  (= pattern (re-find (re-pattern pattern) source)))

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

(describe "#respond-to-request"
          (before (def tracker (atom []))
                  (def client-reader (mock-client-reader full-header))
                  (def client-writer (mock-client-writer tracker))
                  (respond-to-request client-reader client-writer))
          (it "prints the host name"
              (should= "localhost" (some #{"localhost"} @tracker)))
          (it "prints the port number"
              (should= "8080" (some #{"8080"} @tracker)))
          (it "prints the request path"
              (should= "/foo/bar" (some #{"/foo/bar"} @tracker))))

(describe "#map-request-fields"
          (before (def request-fields (map-request-fields header-lines)))
          (it "maps the host"
              (should= "localhost:8080" (:host request-fields)))
          (it "maps the path"
              (should= "/foo/bar" (:path request-fields))))

(describe "#get-response-body"
          (before (def response-body (get-response-body 
                                       (map-request-fields header-lines))))
          (it "includes the host name"
              (should (string-contains? "<p>localhost</p>" response-body)))
          (it "includes the port"
              (should (string-contains? "<p>8080</p>" response-body)))
          (it "includes the path"
              (should (string-contains? "<p>/foo/bar</p>" response-body))))

(describe "#get-response-map"
          (before-all (generate-request)
                  (generate-response))
          (it "maps the status code to 200"
              (should= 200 (:status response-map)))
          (it "maps the host to 'localhost:8080'"
              (should= "localhost:8080" (:host response-map)))
          (it "maps the content-type to 'text/html'"
              (should= "text/html" (:content-type response-map)))
          (it "maps the content-length to the length of the body"
              (should= (.length response-body) (:content-length response-map))))

(describe "#get-response-header"
          (before-all (generate-request)
                      (generate-response))
          (it "includes the status code"
              (should (string-contains? "HTTP/1.1 200 OK" response-header)))
          (it "includes the host"
              (should (string-contains? "Host: localhost:8080" response-header)))
          (it "includes the content length"
              (should (string-contains? "Content-Type: text/html" response-header)))
          (it "includes the content length"
              (should (string-contains? (str "Content-Length: " 
                                             (:content-length response-map))
                                        response-header))))

(describe "#print-response"
          (before-all (def tracker (atom []))
                      (def client-reader (mock-client-reader full-header))
                      (def client-writer (mock-client-writer tracker))
                      (generate-request)
                      (generate-response)
                      (def response (str response-header "\r\n" response-body))
                      (print-response client-reader client-writer)) 
          (it "merges the response header and body"
              (should= response (some #{response} @tracker))))
