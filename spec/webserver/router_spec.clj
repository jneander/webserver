(ns webserver.router-spec
  (:require [webserver.router :refer :all]
            [speclj.core :refer [describe it should=]]))

(defn- stub-request [path]
  {:type "GET" :path path
   :http "1.1" :directory "."})

(describe "#route-request"

  (it "sends directory requests to directory-response"
    (let [response (route-request 
                     (stub-request "/spec/public_html/sample_directory"))]
      (should= (str "<p><a href=\"/spec/public_html/sample_directory/"
                    "sample.txt\">sample.txt</a></p>")
               (:body response))))

  (it "routes '/form' to ok-response"
    (let [response (route-request {:path "/form"})]
      (should= 200 (:status response))))
  
  (it "routes '/redirect' to redirect-response"
    (let [response (route-request (stub-request "/redirect"))]
      (should= 302 (:status response)))))
