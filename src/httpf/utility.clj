(ns httpf.utility
  (:require
   [clojure.set :refer [union]]
   [clojure.string :as str]
   [httpf.content-type :as content-type]
   [httpf.convert :as convert]
   [medley.core :refer [map-vals]]
   [ring.util.response :refer [get-header]]))

(defn log
  "Logs a string."
  [& more]
  (apply println more))

(defn plog
  "Pretty-prints a string to the log. Short for 'pretty log'."
  [object]
  (println (clojure.pprint/write object :stream nil)))

(defn map-invert-with
  "Returns the map with (f each-val) mapped to the keys. Based on
  clojure.test/map-invert."
  [m f]
  (reduce (fn [m [k v]] (assoc m (f v) k)) {} m))

(defmacro lazy-get
  "Like get, but doesn't evaluate not-found unless it is needed."
  [m k not-found]
  `(if-let [pair# (find ~m ~k)]
     (val pair#)
     ~not-found))

(defn safe-get
  "Like get, but throws an exception if the key is not found."
  [m k]
  (lazy-get m k (throw (IllegalArgumentException.
                        (format "Key %s not found in %s" k m)))))

(defn safe-get-in
  "Like get-in, but throws an exception if the key is not found."
  [m ks]
  (reduce safe-get m ks))

(defn method<-ctx
  "Return the request method from the context."
  [ctx]
  (get-in ctx [:request :request-method]))

(defn method=
  "Does the context's request method match the specified method (which
  must be a keyword)?"
  [ctx method]
  {:pre [(keyword? method)]}
  (= method (method<-ctx ctx)))

(defn methods=
  "Does the context's request method match one of the specified
  methods (which must be a set)?"
  [ctx methods]
  {:pre [(set? methods)]}
  (methods (method<-ctx ctx)))

(defn map-group
  [group m]
  (map-vals (fn [x] {:name x :group group}) m))

(defn trace
  "Updates the context by adding the value vector (with the second
  element coerced to a boolean, if present) to the end of
  the :trace. Converting the second part to a boolean makes the trace
  easier to read. It also prevents leakage of unexpected and/or
  unserializable values into the X-httpf-Trace header."
  [ctx val]
  {:pre [(vector? val)]}
  (let [val' (condp = (count val)
               1 val
               2 [(first val) (boolean (second val))]
               (throw (ex-info "Unexpected trace value" {:val val})))]
    (update-in ctx [:trace] conj val')))

(defn trace-header
  "Returns a map that shows the trace through the flow chart,
  suitable for merging into a response header."
  [ctx]
  {"X-httpf-Trace" (map #(str/join " " %) (:trace ctx))})

(defn request-content-type<-ctx
  "Returns the Content-Type header from the :request context, in a
  case-insensitive fashion."
  [ctx]
  (get-header (:request ctx) "content-type"))

(defn response-content-type<-ctx
  "Returns the Content-Type header from the :response context, in a
  case-insensitive fashion."
  [ctx]
  (get-header (:response ctx) "content-type"))

;; TODO: Make this extensible to any content type.
;; TODO: Add HTML, JSONP, and XML.
(defn body-response
  "Converts value into a format useful for :body given the
  context. Expects the context :response to have a non-nil Content-Type
  header."
  [ctx val]
  (if val
    (let [ct (response-content-type<-ctx ctx)]
      (assert ct)
      (cond
        (content-type/edn? ct) (convert/->edn ctx val)
        (content-type/json? ct) (convert/->json ctx val)
        (content-type/text? ct) (convert/->text ctx val)
        :else val))))

(defn ensure-content-type
  "If the response Content-Type header is already set, returns the
  context unchanged. If the content type is missing (or empty), sets it
  to a default and returns the updated context."
  [ctx]
  (if-let [ct (response-content-type<-ctx ctx)]
    ctx
    (let [ct' (or (get-in ctx [:defaults :content-type]) "text/plain")]
      (assoc-in ctx [:response :headers "Content-Type"] ct'))))

(defn response
  "Returns a response with given status code and body (converted to the
  appropriate content type based on the context)."
  [ctx status body]
  (let [ctx' (ensure-content-type ctx)]
    {:status status
     :headers (-> (get-in ctx' [:response :headers])
                  (merge (trace-header ctx')))
     :body (body-response ctx' body)}))

(defn default-response-body
  "Returns a default response body for a context and value. Without
  additional customization to the context, simply returns the value in
  the body. With customization of [:defaults :handler-transform],
  returns the effect of (xform ctx value)."
  [ctx value]
  (if-let [xform (get-in ctx [:defaults :handler-transform])]
    (xform ctx value)
    value))

(defn delegate-response
  "Returns a response with given status code and the body returned from
  a corresponding handler function (if present) or a default body (if
  not) generated from the `default-response-body` function. The body
  will be converted to the appropriate content type based on the
  context. (Note: the supplied `default-body-value` may ultimately
  comprise the entire body, but it may also be transformed; e.g. wrapped
  in a :message key, depending on what context defaults are provided.)"
  [ctx code default-value handler]
  {:pre [(keyword? handler)]}
  (let [body (if-let [h (get-in ctx [:handlers handler])]
               (h ctx)
               (default-response-body ctx default-value))]
    (response ctx code body)))
