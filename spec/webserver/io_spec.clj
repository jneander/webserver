(ns webserver.io-spec
  (:require [webserver.io :refer :all]
            [speclj.core :refer [describe it should=]])
  (:import [java.io File]))

(describe "#read-file"
  
  (it "reads a text file"
    (let [file (File. "./spec/public_html/sample.txt")]
      (should= "foobar\n" (read-file file)))))
