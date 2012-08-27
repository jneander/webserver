(ns webserver.response-test
  (:require [webserver.response :refer :all]
            [speclj.core :refer [describe it should=]])
  (:import [java.io File]))

(defn- test-file-path []
  "spec/public_html/")

(describe "#not-found"
  (it "returns 'not found' response"
    (let [response (not-found)]
      (should= 404 (:status response)))))

(describe "#resource-response"

  (it "returns the body of the specified file"
    (let [response (resource-response "sample.txt" (test-file-path))]
      (should= 200 (:status response))
      (should= "foobar\n" (:body response))))

  (it "returns a list of directory contents for valid directory"
    (let [response (resource-response "/" (test-file-path))
          dir-list (str "<p>file1.txt</p><p>file2.txt</p>"
                        "<p>sample.txt</p><p>sample_directory</p>")]
      (should= 200 (:status response))
      (should= dir-list (:body response))))

  (it "returns 'not found' response for invalid resource"
    (let [response (resource-response "does-not-exist.txt" (test-file-path))]
      (should= 404 (:status response)))))

(describe "#res-response"

  (it "returns the body of the specified file"
    (let [request {:path (str (test-file-path) "sample.txt")}
          response (res-response request)]
      (should= 200 (:status response))
      (should= "foobar\n" (:body response))))

  (it "returns a list of directory contents for valid directory"
    (let [request {:path (test-file-path)}
          response (res-response request)
          dir-list (str "<p>file1.txt</p><p>file2.txt</p>"
                        "<p>sample.txt</p><p>sample_directory</p>")]
      (should= 200 (:status response))
      (should= dir-list (:body response))))

  (it "returns 'not found' response for invalid resource"
    (let [request {:path (str (test-file-path) "does-not-exist.txt")}
          response (res-response request)]
      (should= 404 (:status response)))))
