(ns webserver.request-test
  (:require [webserver.request :refer :all]
            [speclj.core :refer [describe it should=]]))

(defn- request-header []
  (str "GET /foo/bar HTTP/1.1\n"
       "Host: localhost:8080\n"))

(describe "#map-request"

  (it "maps a root-level GET request"
    (let [raw-request "GET / HTTP/1.1"
          request (map-request raw-request)]
      (should= "GET" (:type request))
      (should= "/" (:path request))
      (should= "1.1" (:http request))))

  (it "maps different request types"
    (should= "PUT" (:type (map-request "PUT / HTTP/1.1")))
    (should= "POST" (:type (map-request "POST / HTTP/1.1")))
    (should= "HEAD" (:type (map-request "HEAD / HTTP/1.1"))))

  (it "maps a nested directory request"
    (let [raw-request "PUT /stuff/more/stop/ HTTP/1.0"
          request (map-request raw-request)]
      (should= "PUT" (:type request))
      (should= "/stuff/more/stop/" (:path request))
      (should= "1.0" (:http request))))

  (it "maps the host using full path"
    (let [request (map-request (request-header))]
      (should= "http://localhost:8080" (:host request)))))
