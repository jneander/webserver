(ns webserver.request)

(defn- qualify-host [host]
  (if-not (or (= "http://" (subs host 0 7))
              (= "https://" (subs host 0 8)))
    (str "http://" host)
    host))

(defn- request-type [raw-request]
  (re-find #"GET|PUT|POST|HEAD" raw-request))

(defn- request-path [raw-request]
  (last (re-find #" (.*) HTTP" raw-request))) 

(defn- request-http [raw-request]
  (last (re-find #"HTTP/(.*)" raw-request)))

(defn- request-host [raw-request]
  (if-let [host (last (re-find #"Host: (.*)" raw-request))]
    (qualify-host host)))

(defn map-request [raw-request]
  {:type (request-type raw-request)
   :path (request-path raw-request)
   :http (request-http raw-request)
   :host (request-host raw-request)})
