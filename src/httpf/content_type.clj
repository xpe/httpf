(ns httpf.content-type)

(defn edn?
  "Is the content-type edn?"
  [ct]
  (re-find #"^application/edn" ct))

(defn html?
  "Is the content-type JSONP?"
  [ct]
  (re-find #"^text/html" ct))

(defn json?
  "Is the content-type JSON?"
  [ct]
  (re-find #"^application/json" ct))

(defn jsonp?
  "Is the content-type JSONP?"
  [ct]
  (re-find #"^application/javascript" ct))

(defn text?
  "Is the content-type text?"
  [ct]
  (re-find #"^text/plain" ct))

(defn xml?
  "Is the content-type JSONP?"
  [ct]
  (re-find #"^application/xml" ct))
