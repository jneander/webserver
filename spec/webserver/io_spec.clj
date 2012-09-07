(ns webserver.io-spec
  (:require [webserver.io :refer :all]
            [speclj.core :refer [describe it should=]])
  (:import [java.io File BufferedReader PrintStream]
           [java.net Socket]))

(let [input-stream (proxy [java.io.StringBufferInputStream] ["test-input"])
      output-stream (proxy [java.io.ByteArrayOutputStream] [])]
  (defn- mock-socket []
    (proxy [Socket] []
      (getInputStream [] input-stream)
      (getOutputStream [] output-stream)))
  (defn- reset-output []
    (.reset output-stream)))

(describe "#open-string-reader"

  (it "returns a BufferedReader connected to the socket"
    (with-open [socket (mock-socket)
                reader (open-string-reader socket)]
      (should= BufferedReader (class reader))
      (should= "test-input" (.readLine reader)))))

(describe "#open-string-writer"

  (it "returns a PrintStream connected to the socket"
    (with-open [socket (mock-socket)
                writer (open-string-writer socket)]
      (should= PrintStream (class writer))
      (.print writer "test-output")
      (should= "test-output" (.toString (.getOutputStream socket))))))

(describe "#read-file"
  
  (it "reads a text file"
    (let [file (File. "./spec/public_html/sample.txt")]
      (should= "foobar\n" (read-file file)))))
