(ns webserver.request-handler)

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
