(ns artist-pages.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [artist-pages.views :as views]
            [artist-pages.config :as conf]
            [ring.util.response :as resp]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(def hb-page "http://www.humblebeast.com")

(def config (conf/readcfg))

(defroutes app-routes
  (GET "/" [] (resp/redirect hb-page))
  (GET "/:artist" [artist] 
       (let [conf ((keyword artist) (:artists config))]
         (if (seq conf)
           (views/artist conf)
           (resp/redirect hb-page))
         ))
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))
