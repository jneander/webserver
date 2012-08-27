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
    (let [request {:path (str (test-file-path) "sample.txt")}
          response (resource-response request)]
      (should= 200 (:status response))
      (should= "foobar\n" (:body response))))

  (it "returns a list of directory contents for valid directory"
    (let [request {:path (test-file-path)}
          response (resource-response request)
          dir-list (str "<p>file1.txt</p><p>file2.txt</p>"
                        "<p>sample.txt</p><p>sample_directory</p>")]
      (should= 200 (:status response))
      (should= dir-list (:body response))))

  (it "returns 'not found' response for invalid resource"
    (let [request {:path (str (test-file-path) "does-not-exist.txt")}
          response (resource-response request)]
      (should= 404 (:status response))))
  
  (it "populates the header"
    (let [request {:path (str (test-file-path) "sample.txt")}
          response (resource-response request)]
      (should= 7 (:content-length (:header response))))))
