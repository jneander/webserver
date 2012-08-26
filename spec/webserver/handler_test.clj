(ns webserver.handler-test
  (:require [webserver.handler :refer :all]
            [speclj.core :refer [describe it should=]]))

(defn- request [path]
  {:type "GET" :path (str "/spec/public_html" path) :http "1.1"})

(describe "#route-request"
  (it "sends directory requests to directory-response"
    (let [response (route-request (request "/sample_directory"))]
      (should= "<p>file3.txt</p>" (:body response)))))
