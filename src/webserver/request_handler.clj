(ns webserver.request-handler
  (:require [webserver.request :refer [map-request]]
            [webserver.response :refer [resource-response]]))

(defn read-request-header [client-reader]
  (loop [header "" line (.readLine client-reader)]
    (if (< 0 (.length line))
      (recur (str header line "\r\n") (.readLine client-reader))
      header)))

(defn parse-get-request [string]
  ((fn [string] (if (not (nil? string)) (last string)))
     (re-find #"^GET (.*) HTTP" string)))

(defn get-header-lines [client-reader]
  (loop [prev nil line (.readLine client-reader) lines []] 
    (if (< 0 (.length line)) 
      (recur line (.readLine client-reader) (conj lines line))
      lines)))

(defn get-host [header-lines]
  (loop [lines header-lines]
    (if-not (empty? lines)
      (let [host-value (re-find #"^Host: (.*)" (first lines))]
        (if (nil? host-value)
          (recur (rest lines))
          (last host-value))))))

(defn respond-to-request [client-reader client-writer]
  (let [header-lines (get-header-lines client-reader)]
    (let [[host port] (clojure.string/split (get-host header-lines) #":")
          path (parse-get-request (first header-lines))]
      (.println client-writer host)
      (.println client-writer port)
      (.println client-writer path))))

(defn map-request-fields [header-lines]
  (map-request (clojure.string/join "\r\n"  header-lines)))

(defn get-response-body [request-map]
  (let [[host-name host-port] (clojure.string/split (:host request-map) #":")]
    (str "<p>" host-name "</p>"
         "<p>" host-port "</p>"
         "<p>" (:path request-map) "</p>")))

(defn get-response-map [header-lines response-body]
  {:status 200,
   :host (get-host header-lines),
   :content-type "text/html",
   :content-length (.length response-body)})

(defn get-response-header [response-map]
  (str "HTTP/1.1 200 OK\r\n"
       "Host: " (:host response-map) "\r\n"
       "Content-Type: " (:content-type response-map) "\r\n"
       "Content-Length: " (:content-length response-map) "\r\n"))

(defn print-response [client-reader client-writer]
  (let [header-lines (get-header-lines client-reader)
        request-fields (map-request-fields header-lines)
        response-body (get-response-body request-fields)
        response-map (get-response-map header-lines response-body)
        response-header (get-response-header response-map)]
    (.println client-writer (str response-header "\r\n" response-body))))
