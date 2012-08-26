(ns webserver.handler
  (:require [webserver.response :refer [directory-response]])
  (:import [java.io File]))

(defn- server-directory []
  (.getCanonicalPath (File. ".")))

(defmulti route-request :path)

(defmethod route-request :default [request]
  (directory-response (:path request) (server-directory)))
