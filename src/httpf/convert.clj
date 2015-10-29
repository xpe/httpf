(ns httpf.convert
  (:import
   [com.fasterxml.jackson.core JsonGenerationException])
  (:require
   [cheshire.core :as json]))

(defn ->edn
  "Returns an edn representation of value."
  [ctx val]
  (pr-str val))

(defn prepare-json-conversion
  "Returns a Clojure representation of value that is ready to be
  converted to JSON. Assumes that value is not already in a form
  suitable for JSON."
  [ctx val]
  (if-let [f (get-in ctx [:defaults :prepare-json])]
    (f ctx val)
    (throw (ex-info (str "Value not ready to be converted to JSON. "
                         "Cannot find converter function in "
                         "[:defaults :prepare-json].")
                    {:value val}))))

(defn ->json
  "Returns a JSON representation of value. If Cheshire cannot convert
  the JSON, delegates to `prepare-json-conversion`; if that fails, lets
  the underlying `JsonGenerationException` bubble up."
  [ctx val]
  (try
    (json/generate-string val)
    (catch JsonGenerationException e
      (-> (prepare-json-conversion ctx val)
          json/generate-string))))

(defn ->text
  "Returns a text representation of value."
  [ctx val]
  (str val))
