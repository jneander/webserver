(ns webserver.response-test
  (:require [webserver.response :refer :all]
            [webserver.spec-helper :refer [should-contain]]
            [speclj.core :refer [describe it should= should]])
  (:import [java.io File]))

(defn- test-file-path [file]
  (str "/spec/public_html/" file))

(defn- stub-resource-request [file]
  {:http "1.1"
   :host "localhost:8080"
   :path (str "/spec/public_html/" file)
   :directory "."})

(describe "#not-found"
  (it "returns 'not found' response"
    (let [response (not-found)]
      (should= 404 (:status response)))))

(describe "#resource-response"

  (it "returns the body of the specified file"
    (let [request {:path (test-file-path "sample.txt")
                   :directory "."}
          response (resource-response request)]
      (should= 200 (:status response))
      (should= "foobar\n" (:body response))))

  (it "responds with requested binary content"
    (let [request {:path (test-file-path "image.jpeg")
                   :directory "."}
          response (resource-response request)]
      (should= "image/jpeg" (:content-type (:header response)))
      (should= 200 (:status response))
      (should= 38400 (:content-length (:header response)))))

  (it "prefixes root-level links with single slashes"
    (let [request {:path "/" :directory "."}
          response (resource-response request)
          expected "href=\"/spec\""]
      (should-contain expected (:body response))))

  (it "returns a list of directory contents for valid directory"
    (let [request {:path (test-file-path "") :directory "."}
          response (resource-response request)
          files ["file1.txt" "file2.txt" "sample.txt" "sample_directory"]]
      (should= 200 (:status response))
      (doseq [filename files] 
        (should-contain filename (:body response)))))

  (it "returns a list of directory contents for nested directory"
    (let [request {:path (test-file-path "sample_directory")
                   :directory "."}
          response (resource-response request)
          expected (str "<p><a href=\""
                        (:path request) "/sample.txt\">"
                        "sample.txt</a></p>")]
      (should= expected (:body response))))

  (it "returns 'not found' response for invalid resource"
    (let [request {:path (str (test-file-path "does-not-exist.txt"))
                   :directory "."}
          response (resource-response request)]
      (should= 404 (:status response))
      (should-contain "Not Found" (:status-message (:header response)))))

  (it "uses provided directory"
    (let [ok-request {:path (test-file-path "sample.txt") :directory "."}
          bad-request {:path (test-file-path "sample.txt") :directory "/"}]
      (let [response (resource-response ok-request)]
        (should= 200 (:status response)))
      (let [response (resource-response bad-request)]
        (should= 404 (:status response)))))
  
  (it "populates the header"
    (let [request (stub-resource-request "sample.txt")
          response-header (:header (resource-response request))]
      (should= 7 (:content-length response-header))
      (should= (:host request) (:host response-header))
      (should= "HTTP/1.1 200 OK" (:status-message response-header))))

  (it "describes the data type"
    (let [request (stub-resource-request "image.jpeg")
          response (resource-response request)]
      (should= :binary (:data-type response)))
    (let [request (stub-resource-request "sample.txt")
          response (resource-response request)]
      (should= :text (:data-type response)))
    (let [request (stub-resource-request "")
          response (resource-response request)]
      (should= :text (:data-type response)))))

(describe "#echo-response"

  (it "returns the request in the body"
    (let [request {:path "/foo" :host "localhost:8080" :directory "."}
          response (echo-response request)]
      (should= "text/plain" (:content-type (:header response)))
      (should= "localhost:8080/foo" (:body response))))

  (it "describes the data type"
    (let [request (stub-resource-request "/form")
          response (echo-response request)]
      (should= :text (:data-type response)))))

(describe "#echo-query-response"

  (it "echoes the query variables and values"
    (let [request {:path "/some-script-url?variable_1=123459876&variable_2=some_value"
                   :host "localhost:8080" :directory "."}
          response (echo-query-response request)
          expected (str "variable_1 = 123459876\r\n"
                        "variable_2 = some_value")]
      (should= "text/plain" (:content-type (:header response)))
      (should= expected (:body response))))

  (it "is blank when no parameters are supplied"
    (let [request {:path "/some-script-url"
                   :host "localhost:8080" :directory "."}
          response (echo-query-response request)]
      (should= 200 (:status response))
      (should= "" (:body response))))

  (it "maps no-assignment parameters to 'true'"
    (let [request {:path "/some-script-url?my-param"
                   :host "localhost:8080" :directory "."}
          response (echo-query-response request)]
      (should= "my-param = true" (:body response))))

  (it "describes the data type"
    (let [request (stub-resource-request "/some-script-url?foo=bar")
          response (echo-query-response request)]
      (should= :text (:data-type response)))))

(describe "#redirect-response"

  (it "returns a redirect response"
    (let [request {:http 1.1 :path "/redirect" 
                   :host "localhost:8080" :directory "."}
          response (redirect-response request "/")
          header (:header response)]
      (should= 302 (:status response))
      (should= "/" (:location header))
      (should= :text (:data-type response))
      (should= "HTTP/1.1 302 Found" (:status-message header))
      (should= "localhost:8080" (:host header))
      (should= 0 (:content-length header))
      (should= "text/html" (:content-type header))
      (should= "" (:body response)))))
