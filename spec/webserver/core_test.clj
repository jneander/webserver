(ns webserver.core-test
  (:require [speclj.core :refer [describe it should=]]
            [webserver.core :refer [parse-arguments]]))

(describe "#parse-arguments"

  (it "parses acceptable values"
    (let [args ["-p" "7000" "-d" "/foo/bar"]
          parsed (parse-arguments args)]
      (should= 7000 (:port parsed))
      (should= "/foo/bar" (:directory parsed))))

  (it "uses default values"
    (let [args ["p" "7000" "d" "/foo/bar"]
          parsed (parse-arguments args)]
      (should= 8080 (:port parsed))
      (should= "." (:directory parsed)))))
