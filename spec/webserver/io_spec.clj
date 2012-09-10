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

(describe "#get-ext"

  (it "returns the extension of files"
    (should= "txt" (get-ext (File. "./sample.txt")))
    (should= "jpeg" (get-ext (File. "./image.jpeg"))))

  (it "returns nil for files with no extension"
    (should= nil (get-ext (File. "no-extension")))))

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

(describe "#read-file"

  (it "reads a text file"
    (let [content (read-file (File. "./spec/public_html/sample.txt"))]
      (should= "foobar\n" (:body content))
      (should= 7 (:length content))
      (should= :text (:data-type content))))

  (it "reads a binary file"
    (let [content (read-file (File. "./spec/public_html/image.jpeg"))]
      (should= (class (byte-array [(byte 1)])) (class (:body content)))
      (should= 38400 (:length content))
      (should= :binary (:data-type content)))))
