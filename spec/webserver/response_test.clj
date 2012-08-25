(ns webserver.response-test
  (:require [webserver.response :refer :all]
            [speclj.core :refer [describe it should=]])
  (:import [java.io File]))

(defn- test-file-path []
  "spec/public_html")

(describe "#not-found"
  (it "returns 'not found' response"
    (let [response (not-found)]
      (should= 404 (:status response)))))

(describe "#file-response"
  (it "returns file response map for valid file"
    (let [response (file-response "sample.txt" (test-file-path))]
      (should= (class {}) (class response))))
  (it "returns 'not-found' response for invalid file"
    (let [response (file-response "does-not-exist.txt" (test-file-path))]
      (should= (not-found) response))))
