(ns webserver.handler
  (:require [webserver.response :refer [resource-response]])
  (:import [java.io File]))

(defn- server-directory []
  (.getCanonicalPath (File. ".")))

(defmulti route-request :path)

(defmethod route-request :default [request]
  (resource-response request))
