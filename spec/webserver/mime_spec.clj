(ns webserver.mime-spec
  (:require [webserver.mime :refer :all]
            [speclj.core :refer [describe it should=]]))

(describe "#ext-to-mime-type"

  (it "maps the following extensions to their MIME types"
    (should= "text/plain" (ext-to-mime-type "txt"))
    (should= "text/html" (ext-to-mime-type "html"))
    (should= "image/jpeg" (ext-to-mime-type "jpeg")))
  
  (it "maps unknown extensions to 'text/plain' by default"
    (should= "application/octet-stream" (ext-to-mime-type "unknown"))))

(describe "#ext-to-data-type"

  (it "maps the following extensions to their data types"
    (should= :text (ext-to-data-type "txt"))
    (should= :text (ext-to-data-type "html"))
    (should= :binary (ext-to-data-type "jpeg")))
  
  (it "maps unknown extensions to :binary by default"
    (should= :binary (ext-to-data-type "unknown"))))

(describe "#mime-to-data-type"

  (it "maps the following MIME types to their data types"
    (should= :text (mime-to-data-type "text/plain"))
    (should= :text (mime-to-data-type "text/html"))
    (should= :binary (mime-to-data-type "image/jpeg")))

  (it "maps unknown types to :binary by default"
    (should= :binary (mime-to-data-type "unknown"))))
