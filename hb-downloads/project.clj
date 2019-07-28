(defproject hb-downloads "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [compojure "1.4.0"]
                 [ring/ring-defaults "0.1.5"]
                 [ring/ring-json "0.4.0"]
                 [org.clojure/java.jdbc "0.4.2"]
                 [mysql/mysql-connector-java "5.1.38"]
                 [korma "0.4.0"]
                 [clojchimp "1.0.1"]
                 [org.clojars.tnoda/simple-crypto "0.1.0"]
                 [crypto-random "1.2.0"]
                 [com.draines/postal "1.11.3"]
                 [amazonica "0.3.51"]
                 [clj-fuzzy "0.1.8"]
                 [clj-time "0.11.0"]
                 [amalloy/ring-gzip-middleware "0.1.3"]]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler hb-downloads.handler/app
         :auto-reload? false
         :auto-refresh? false
         }
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
