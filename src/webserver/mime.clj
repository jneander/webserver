(ns webserver.mime)

(defn- mime-types []
  {"text/plain" {:ext ["txt"] :data-type :text}
   "text/html" {:ext ["html"] :data-type :text}
   "image/jpeg" {:ext ["jpg" "jpeg"] :data-type :binary}})

(defn ext-to-mime-type [ext]
  (or (first (for [[k v] (mime-types) 
                   :when (some #{ext} (:ext v))] k))
      "application/octet-stream"))

(defn ext-to-data-type [ext]
  (or (first (for [[k v] (mime-types) 
                   :when (some #{ext} (:ext v))] (:data-type v)))
      :binary))

(defn mime-to-data-type [mime]
  (if (contains? (mime-types) mime)
    (:data-type (get (mime-types) mime))
    :binary))
