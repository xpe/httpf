(defproject httpf "0.0.1-SNAPSHOT"
  :description "Better HTTP services with declarative programming."
  :url "http://github.com/bluemont/httpf"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies
  [[cheshire "5.5.0"]
   [medley "0.7.0"]
   [org.clojure/clojure "1.7.0"]
   [ring "1.4.0" :exclusions [org.clojure/java.classpath]]]
  :profiles
  {:dev
   {:source-paths ["dev"]
    :dependencies [[org.clojure/tools.namespace "0.2.10"]]}})
