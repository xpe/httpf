(ns httpf.core
  (:require
   [httpf.decisions]
   [httpf.graph :as graph]
   [httpf.handlers]
   [httpf.utility :refer [log plog] :as u]))

(defn transform-fn
  "Returns a transformation function for a given node keyword and
  context. If there is no corresponding function, returns `identity`."
  [node-kw ctx]
  {:pre [(keyword? node-kw) (map? ctx)]}
  (let [transform-kw node-kw]
    (get-in ctx [:transforms transform-kw] identity)))

(defn decision-fn
  "Returns a decision function for a given node keyword and context. If
  the context does not contain a corresponding keyword, looks up the
  decision function from the `httpf.decisions` namespace."
  [node-kw ctx]
  {:pre [(keyword? node-kw) (map? ctx)]}
  (let [decision-kw (keyword (str (name node-kw) "?"))]
    (log "> decision-fn: decision-kw" decision-kw)
    (if-let [f (get-in ctx [:decisions decision-kw])]
      f
      (let [sym (symbol "httpf.decisions" (name decision-kw))]
        (log "> decision-fn: using" sym)
        (find-var sym)))))

(defn handler-fn
  "Returns a handler function for a given node keyword and context. If
  the context does not contain a corresponding keyword, looks up the
  decision function from the `httpf.decisions` namespace."
  [handler-kw ctx]
  {:pre [(keyword? handler-kw) (nil? (namespace handler-kw)) (map? ctx)]}
  (if-let [f (get-in ctx [:handlers handler-kw])]
    f
    (let [sym (symbol "httpf.handlers" (name handler-kw))]
      (log "> handler-fn: using" sym)
      (find-var sym))))

(defn next-node-kw
  "Returns the next keyword in the flowchart."
  [node-kw result {:keys [edges labels-by-nodes nodes] :as ctx}]
  {:pre [(keyword? node-kw) (map? ctx)]}
  (let [label-kw (u/safe-get labels-by-nodes node-kw)
        node-kw' (u/safe-get edges [label-kw result])]
    (if (namespace node-kw')
      node-kw'
      (u/safe-get-in nodes [node-kw' :name]))))

(defn terminal-node-fn
  "Returns a terminal node (i.e. a handler) function for a given
  keyword."
  [node-kw]
  {:pre [(= (namespace node-kw) "handler")]}
  (log "> terminal-node-fn: node-kw" node-kw)
  (fn [ctx]
    (let [handler-kw (keyword (name node-kw))
          _ (log "> terminal-node-fn: handler-kw" handler-kw)
          next-fn (handler-fn handler-kw ctx)]
      (next-fn ctx))))

(declare node-fn)

(defn decision-node-fn
  "Returns a decision node function for a given keyword."
  [node-kw]
  (log "> decision-node-fn: node-kw" node-kw)
  (fn [ctx]
    (let [xform-fn (transform-fn node-kw ctx)
          ctx' (xform-fn ctx)
          result (boolean ((decision-fn node-kw ctx') ctx'))
          _ (log "> decision-node-fn: result" result)
          node-kw' (next-node-kw node-kw result ctx')
          next-fn (node-fn node-kw')]
      (next-fn ctx'))))

(defn node-fn
  "Returns a node function for a given keyword."
  [node-kw]
  (log "\n> node-fn: node-kw" node-kw)
  (if (namespace node-kw)
    (terminal-node-fn node-kw)
    (decision-node-fn node-kw)))

(defn resource
  "Returns an httpf resource, a special kind of Ring handler. The config
  argument must be a map, and usually will include the `:transforms`,
  `:decisions`, and `:handlers` keys. As a last resort, the flow chart
  itself can be customized by using the `:nodes` and `:edges` keys."
  [{:keys [transforms decisions handlers nodes edges] :as config}]
  (let [nodes' (merge (graph/nodes) nodes)
        edges' (merge (graph/edges) edges)
        context {:transforms transforms
                 :decisions decisions
                 :handlers handlers
                 :nodes nodes'
                 :edges edges'
                 :labels-by-nodes (u/map-invert-with nodes' :name)
                 :trace []}]
    (fn [request]
      (let [ctx (merge context {:request request})
            f (node-fn :start)]
        (f ctx)))))
