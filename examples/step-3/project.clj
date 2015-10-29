(defproject step-3 "0.1.0-SNAPSHOT"
  :description "Step 3 of an example app for httpf."
  :url "http://github.com/bluemont/httpf"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies
  [[cheshire "5.5.0"]
   [httpf "0.0.1-SNAPSHOT"]
   [medley "0.7.0"]
   [slingshot "0.12.2"]
   [org.clojure/clojure "1.7.0"]
   [ring "1.4.0" :exclusions [org.clojure/java.classpath]]]
  :ring {:handler step-3.app/handler}
  :plugins [[lein-ring "0.9.6"]]
  :profiles
  {:dev
   {:source-paths ["dev"]
    :dependencies [[org.clojure/tools.namespace "0.2.10"]]}})
