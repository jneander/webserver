(ns webserver.core-test
  (:use speclj.core
        webserver.core))

(describe "open-server-socket"
          (before (def socket (open-server-socket 8080)))
          (after (.close socket))
          (it "returns a ServerSocket instance"
              (should= (.getClass socket) java.net.ServerSocket))
          (it "uses the provided port"
              (should= (.getLocalPort socket) 8080)))
