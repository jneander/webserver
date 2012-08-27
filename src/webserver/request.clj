(ns webserver.request)

(defn- request-type [raw-request]
  (re-find #"GET|PUT|POST|HEAD" raw-request))

(defn- request-path [raw-request]
  (last (re-find #" (.*) HTTP" raw-request))) 

(defn- request-http [raw-request]
  (last (re-find #"HTTP/(.*)" raw-request)))

(defn- request-host [raw-request]
  (last (re-find #"Host: (.*)" raw-request)))

(defn map-request [raw-request]
  {:type (request-type raw-request)
   :path (request-path raw-request)
   :http (request-http raw-request)
   :host (request-host raw-request)})
