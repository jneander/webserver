(defproject webserver "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]]
  :plugins [[speclj "2.3.0"]]
  :profiles {:dev {:dependencies [[speclj "2.3.0"]]}}
  :test-paths ["spec/"]
  :main ^:skip-aot webserver.core)
