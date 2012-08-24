(ns webserver.request-test
  (:require [webserver.request :refer :all]
            [speclj.core :refer [describe it should=]]))

(describe "#map-request"
  (it "maps a root-level GET request"
    (let [raw-request "GET / HTTP/1.1"
          request (map-request raw-request)]
      (should= "GET" (:type request))
      (should= "/" (:path request))
      (should= "1.1" (:http request))))
  (it "maps a nested directory request"
    (let [raw-request "PUT /stuff/more/stop/ HTTP/1.0"
          request (map-request raw-request)]
      (should= "PUT" (:type request))
      (should= "/stuff/more/stop/" (:path request))
      (should= "1.0" (:http request)))))
