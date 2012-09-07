(ns webserver.router
  (:require [webserver.response :refer [resource-response
                                        echo-response
                                        echo-query-response]]
            [clojure.string :refer [split]]))

(defn- get-route [request]
  (first (split (:path request) #"\?")))

(defmulti route-request get-route)

(defmethod route-request "/form" [request]
  (echo-response request))

(defmethod route-request "/some-script-url" [request]
  (echo-query-response request))

(defmethod route-request :default [request]
  (resource-response request))
