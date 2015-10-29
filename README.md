# httpf

Using common programming tools can make it difficult to write an HTTP service
that correctly and consistently handles status codes, headers, and
responses. This library, httpf (the 'f' means flowchart), provides a simple way
to write cleaner, more correct HTTP services in Clojure. To use httpf, you write
code in a declarative style.

## Dependency

Add this to your project.clj:

```clj
[httpf "0.0.1-SNAPSHOT"]
```

## Examples

### How to Run The Examples

To run step 3 of the example:

```sh
cd examples/step-3
lein ring server
```

Then try out different URLs in a web browser, such as:

* http://localhost:3000/games/5
* http://localhost:3000/games/25
* http://localhost:3000/games/78125
* http://localhost:3000/games/48828125
* http://localhost:3000/games/1220703125

### Example Code Walkthrough

Here is a minimal example to demonstrate a minimal setup for httpf with
Compojure. The code for each step in the walkthrough is located in the
'examples' directory.

```clj
(ns step-1.app
  (:require
   [compojure.core :refer [defroutes ANY]]
   [httpf.core :as httpf]
   [ring.middleware.params :refer [wrap-params]]
   [slingshot.slingshot :refer [throw+]]))

(def games-resource
  (httpf/resource {}))

(defroutes routes
  (ANY "/games/:id" _ games-resource))

(def handler
  (wrap-params routes))
```

The above code doesn't do anything useful; it only shows a bare-bones starting
point for httpf. You will need to customize your httpf resource by adding
additional information to the `resource` map argument:

```clj
(def games-resource
  (httpf/resource
   {:transforms {}
    :decisions {}
    :handlers {}}))
```

The map keys (`:transforms`, `:decisions`, and `:handlers`) are explained next,
in reverse order:

#### Handlers

Handlers are terminal nodes in the flow chart. Examples include 404 Not Found
(called `:not-found`) and 200 Ok (called `:ok`). For example, to customize the
404 Not Found response body, nest a map under the `:handlers` key:

```clj
;; step-2.app (in progress)
(def games-resource
  (httpf/resource
   {:handlers
    {:not-found (fn [ctx] "No Game Found")}}))
```

Handlers are functions that accept a context (a map containing a `:request`, an
in-progress `:response`, and other data that you wish to merge in) and return a
Clojure value that will be converted into a body of a Ring response. The
conversion happens based on the Content-Type header in the :response value of
the context. (The context's `:response` value is the 'pending' response that can
be changed up until the point at which the response is returned via the Ring
handler.)

#### Decisions

There is one decision per decision node in the flow chart. Decisions are
functions that accept a context and return a value that will be interpreted
simply as true or false.

Decisions cannot be used to update the context, by design. I have found that
this leads to cleaner code. If you want to update context, use transforms; see
below.

For example, use `:decisions` to define when a resource is missing. This code
makes ids 25, 26, and 27 go missing:

```clj
;; step-2.app
(defn get-id
  "Return the id, as a long, from the context."
  [ctx]
  (some-> (get-in ctx [:request :params :id]) Long.))

(defn id=
  "Does the :id param match one of the provided ids?"
  [ctx ids]
  {:pre [(set? ids)]}
  (-> ctx get-id ids))

(def games-resource
  (httpf/resource
   {:decisions
    {:missing? #(id= % #{25 26 27})}
    :handlers
    {:not-found (fn [ctx] "No Game Found")}}))
```

Since the example modifies the `:missing?` decision, the flow chart in httpf
will now return a 404 Not Found for id's 25, 26, and 27.

If something exceptional happens, throw an exception and the flow chart will
return immediately:

```clj
;; step-2.app
(defn moved?
  "A made-up example to show how to use exceptions."
  [ctx]
  (if (= 1 (inc (rand-int 6))) ;; a little dicey
    (let [response (httpf.handlers/internal-server-error ctx)]
      (throw+ (assoc response :type :httpf)))
    false))

(def games-resources
  (http/resources
   {:decisions
    {:moved? moved?
     ;; ...
     }
    ;; ...
    }))
```

Exceptions thrown in this way will return the contained map (after removing the
`:type` key) to the Ring handler.

#### Transforms

There is a transform for each decision node in the flow chart. Transforms run
before a decision function for a node in the flow chart.

For example, you could use a transform to merge in a user-id that would be
available for subsequent processing:

```clj
;; step-3.app
(def games-resource
  (httpf/resource
   {:transforms
    {:is-authorized
     (fn [ctx] (assoc ctx :user-id 12345))}}))
```

#### Decision Nodes

Above, we've covered what transforms, decisions, and handlers do. This section
explains how they fit together.

The flow chart in httpf is a directed graph consisting of nodes and edges. There
are two types of nodes: decision nodes and terminal nodes (i.e. handlers).

Each decision node does three things:

1. Calls a custom transform, if present.
2. Calls the corresponding decision function (see section above).
3. Calls the next appropriate node based upon the decision (which may be another
   decision node or a terminal node; i.e. a handler).

All decision nodes in httpf have two possible outbound paths: one for true, one
for false. (A few nodes have decision functions that always returns true, so
they only have one output path.)

## Terminology and Concepts

The sections above introduce the key concepts of httpf by way of a
walkthrough. Here, I restate the key concepts for easier reference.

### Flow Chart

A flow chart is a directed graph consisting of nodes and edges. There are two
kinds of nodes: decision nodes and terminal nodes (the same thing as
handlers). Edges simply represent control flow.

### Context

A context is a data structure used inside the httpf logic. Your
application-specific code will most likely need to transform it. A context a map
containing:

* the request (nested in `:request`)
* an in-progress response (nested in `:response`)
* tracing information for debugging (nested in `:trace`)
* any other data that you wish to merge in.

I recommend that you nest your application-specific data in one key of your
choosing, such as `:app-data`.

### Decision Nodes

Decision nodes do three things:

1. evaluate a transform function
2. evaluate a decision function
3. transfer control to another node

### Handlers (Terminal Nodes)

Handlers are functions that accept a context and return a Clojure value that
will be converted into a body of a Ring response. The conversion happens based
on the Content-Type header in the :response value of the context. (The context's
:response value is the 'pending' response that can be changed up until the point
at which the response is returned via the Ring handler.)

## Commentary and Discussion

### A Flow Chart, Not a State Machine

State machines are nice, but httpf does not use a state machine; a flow chart is
all that is needed.

Some suggest that such an approach is a state machine. Yes, technically, a flow
chart is a degenerate state machine, but I tend to prefer the more specific
term.

A classic example of a state machine is a system controlling a traffic light. It
has internal state that changes over time; it is more than a flow chart. This
[MathWorks documentation][MW] goes into some detail:

[MW]: http://www.mathworks.com/help/stateflow/ug/difference-between-flow-graphs-and-state-charts.html

> **Difference Between Flow Charts and State Transition Diagrams**

> A flow chart is used for combinatorial design. It is a stateless flow chart
> because it cannot maintain its active state between updates. As a result, a
> flow chart always begins executing from a default transition and ends at a
> terminating junction (a junction that has no valid outgoing transitions).

> By contrast, a state transition diagram is used for sequential design. It
> stores its current state in memory to preserve local data and activity between
> updates. As a result, state diagrams can begin executing where they left off
> in the previous time step, making them suitable for modeling reactive or
> supervisory systems that depend on history. In these kinds of systems, the
> current result depends on a previous result.

## Customization

Let's quickly revisit the three parts of a decision node:

1. Calls a custom transformation, if present (customized with `:transforms`).
2. Calls the corresponding decision function (customized with `:decisions`.)
3. Calls the next node based upon the decision; either another decision node or
   a handler (customized with `:handlers`).

If built sensibly, your application likely only needs to customize `:transforms`,
`:decisions`, and `:handlers`.

This is by design; the HTTP Decision Diagram is intended to provide constraints
and make you think differently about your resources. If you do, you might make
wiser tradeoffs and take advantage of the functionality and semantic
possibilities that HTTP makes available: content negotiation, conditional
requests, and caching, just to mention a few.

### Changing the Flow Chart

You might run into a situation where you would like to add a decision node or
rewire the flow chart. httpf allows this; however, do so with caution.

The first rule of customizing the httpf flow chart is this: Don't customize
the httpf flow chart.

With that caveat, here is how you do it:


```clj
(def games-resource
  (httpf/resource
   {:nodes :TODO
    :edges :TODO
    :transforms {}
    :decisions {}
    :handlers {}}))
```

TODO: Explain why `merge` is used.

TODO: Add and explain the `defaults` vars and values.

TODO: Explain how and why metadata is used.

## Pros and Cons of HTTP APIs based on flow charts

When we say a "HTTP APIs based on flow charts" we mean using a reusable flow
chart that breaks apart your application and resource logic into smaller chunks
of decision logic.

Using a flow chart approach has these advantages over coding an HTTP API "by
hand":

* Defines an HTTP API in a more declarative way.
* Provides sensible defaults.
* Reduces errors because of better edge case handling.
* Encourages developers to take advantage of idiomatic HTTP processing and
  status codes.

You may also perceive some drawbacks:

* Using httpf may feel strange at first.
* You will have to restructure your logic in terms of the flow chart.
* You'll need to keep the HTTP Decision Diagram handy and review it regularly.

## Differences from Liberator

httpf is different from [Liberator][L] in these ways:

* httpf is based on the [HTTP Decision Diagram][DD] (HTTPDD) by Andrei Neculau.
* httpf strives to be simpler to reason about and use. Transformations only
  update the context. Decisions only return true or false. Handlers only return
  responses.
* httpf let's you customize the flow chart, if needed.

[L]: https://github.com/clojure-liberator/liberator
[DD]: https://github.com/for-GET/http-decision-diagram

## Goals and Related Projects

I build httpf because I liked the motivation behind liberator but not its
implementation. My goals were:

* Provide a sensible default flow chart covering the HTTP specification, based
upon the well thought-out flow chart from the [HTTP Decision Diagram][DD].
* Provide clean extension points for configurability.
* Allow for changes to the flow chart (but only as a last resort).

## Inspiration

> C                     G         C
> I always thought that you would fly
>
> C                      G
> Sail away and learn to ride
>
> F                                  Am
> But you're waiting for a change in time.
>
> C             F       C       Am             G  C
> Take a train, catch a flight, go ahead it'll be alright
>
> -It'll Be Alright
> -The Infamous Stringdusters

Writing your HTTP APIs in a more declarative way might feel strange. That's
ok. Try something different. You might like it.
