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
