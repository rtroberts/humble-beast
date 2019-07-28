(defproject canvas-video "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [compojure "1.5.1"]
                 [ring/ring-defaults "0.2.1"]
                 [ring/ring-json "0.4.0"]
                 [clj-time "0.11.0"]
                 ;[org.clojure/java.jdbc "0.4.2"]
                 [mysql/mysql-connector-java "5.1.38"]
                 [korma "0.4.0"]
                 [clojchimp "1.0.1"]
                 ]
  :plugins [[lein-ring "0.9.7"]]
  :ring {:handler canvas-video.handler/app
         :port 3001
         :auto-reload? false
         :auto-refresh? false
         }
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.3.0"]]}})
