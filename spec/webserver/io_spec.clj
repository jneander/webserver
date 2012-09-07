(ns webserver.io-spec
  (:require [webserver.io :refer :all]
            [speclj.core :refer [describe it should= should-not=]])
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
      (reset-output)
      (.print writer "test-output")
      (should= PrintStream (class writer))
      (should= "test-output" (.toString (.getOutputStream socket))))))

(describe "#open-binary-writer"

  (it "returns a binary stream connected to the socket"
    (let [socket (mock-socket)
          writer (open-binary-writer socket)
          data (byte-array (map byte (take 30 (repeat 0))))]
      (reset-output)
      (.write writer data)
      (should= java.io.FilterOutputStream (class writer))
      (should= 30 (.size (.getOutputStream socket))))))

(describe "#read-binary-file"

  (it "reads a binary file"
    (let [file (File. "./spec/public_html/image.jpeg")
          read (read-binary-file file)]
      (should= 38400 (:length read))
      (should-not= nil (:body read)))))

(describe "#read-file"

  (it "reads a text file"
    (let [file (File. "./spec/public_html/sample.txt")]
      (should= "foobar\n" (read-file file)))))
