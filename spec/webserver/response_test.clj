(ns webserver.response-test
  (:require [webserver.response :refer :all]
            [speclj.core :refer [describe it should= should]])
  (:import [java.io File]))

(defn- string-contains? [match target]
  (= match (re-find (re-pattern match) target)))

(defn- test-file-path [file]
  (str "spec/public_html/" file))

(describe "#not-found"
  (it "returns 'not found' response"
    (let [response (not-found)]
      (should= 404 (:status response)))))

(describe "#resource-response"

  (it "returns the body of the specified file"
    (let [request {:path (test-file-path "sample.txt")}
          response (resource-response request)]
      (should= 200 (:status response))
      (should= "foobar\n" (:body response))))

  (it "returns a list of directory contents for valid directory"
    (let [request {:path (test-file-path "")}
          response (resource-response request)
          dir-list (str "<p>file1.txt</p><p>file2.txt</p>"
                        "<p>sample.txt</p><p>sample_directory</p>")]
      (should= 200 (:status response))
      (should (string-contains? "OK" (:status-message (:header response))))
      (should= dir-list (:body response))))

  (it "returns 'not found' response for invalid resource"
    (let [request {:path (str (test-file-path "does-not-exist.txt"))}
          response (resource-response request)]
      (should= 404 (:status response))
      (should (string-contains? "Not Found" 
                                (:status-message (:header response))))))

  (it "uses provided directory"
    (let [request {:path (test-file-path "sample.txt")}]
      (let [response (resource-response request ".")]
        (should= 200 (:status response)))
      (let [response (resource-response request "/")]
        (should= 404 (:status response)))))
  
  (it "populates the header"
    (let [request {:http "1.1"
                   :host "localhost:8080"
                   :path (str (test-file-path "sample.txt"))}
          response-header (:header (resource-response request))]
      (should= 7 (:content-length response-header))
      (should= "text/html" (:content-type response-header))
      (should= (:host request) (:host response-header))
      (should= "HTTP/1.1 200 OK" (:status-message response-header)))))

(describe "#echo-response"

  (it "returns the request in the body"
    (let [request {:path "/foo" :host "localhost:8080"}
          response (echo-response request ".")]
      (should= "localhost:8080/foo" (:body response)))))

