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

(describe "#directory-response"
  (it "returns a list of directory contents for valid directory"
    (let [response (directory-response "/" (test-file-path))
          dir-list (str "<p>file1.txt</p><p>file2.txt</p>"
                        "<p>sample.txt</p><p>sample_directory</p>")]
      (should= 200 (:status response))
      (should= {} (:headers response))
      (should= dir-list (:body response))))
  (it "returns 'not found' response for invalid directory"
    (let [response (directory-response "/does-not-exist/" (test-file-path))]
      (should= (not-found) response))))
