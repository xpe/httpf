(ns step-2.app
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

(defn moved?
  "A made-up example to show how to use exceptions."
  [ctx]
  (if (= 1 (inc (rand-int 6))) ;; a little dicey
    (let [response (httpf.handlers/internal-server-error ctx)]
      (throw+ (assoc response :type :httpf)))
    false))

(def games-resource
  (httpf/resource
   {:decisions
    {:missing? #(id= % #{25 26 27})
     :moved? moved?}
    :handlers
    {:not-found (fn [ctx] "No Game Found")}}))

(defroutes routes
  (ANY "/games/:id" _ games-resource))

(def handler
  (wrap-params routes))
