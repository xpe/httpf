(ns step-3.app
  (:require
   [compojure.core :refer [defroutes ANY]]
   [httpf.core :as httpf]
   [ring.middleware.params :refer [wrap-params]]
   [slingshot.slingshot :refer [throw+]]))

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
    {:is-uri-too-long? (fn [ctx]
                         (let [uri (get-in ctx [:request :uri])]
                           (> (count uri) 15)))
     :missing? #(id= % #{25 26 27})}
    :handlers
    {:not-found (fn [ctx] "No Game Found")}}))

(defroutes routes
  (ANY "/games/:id" _ games-resource))

(def handler
  (wrap-params routes))
