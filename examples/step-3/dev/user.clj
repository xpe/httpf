(ns user
  (:require
   [clojure.pprint :refer (pprint)]
   [clojure.repl :refer :all]
   [clojure.string :as str]
   [clojure.test :refer [run-tests run-all-tests]]
   [clojure.tools.namespace.repl :refer (refresh refresh-all)]
   [httpf.core :as httpf]))

(set! *warn-on-reflection* true)
