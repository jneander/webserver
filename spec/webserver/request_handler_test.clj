(ns webserver.request-handler-test
  (:use speclj.core
        webserver.request-handler)
  (:import (java.io BufferedReader StringReader)))

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

(defn mock-client-reader [string]
  (BufferedReader. (StringReader. string)))

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
