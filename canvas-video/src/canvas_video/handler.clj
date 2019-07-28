(ns canvas-video.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [canvas-video.views :as views]
            [canvas-video.database :as database]
            [canvas-video.mailing-list :as mailing-list]
            [ring.middleware.json :refer [wrap-json-body]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defroutes app-routes
  (GET  "/" [] (views/video))
  (POST "/downloaders/create" json-params
        (let [body (:body json-params)
              params (assoc body :onetime_key "canvasconf-key")
              ]
          (do
            (database/create params)
            ;; Should be using a PUT for mailchimp, but the lib doesn't support yet.
            ;; Currently it throws an exception (a 400 I think)
            (try (mailing-list/add-to-mailing-list params)
              (catch Exception e (str "caught exception: " (.getMessage e))))
            )
          )) 
  
  (route/not-found "Not Found"))

(def app
  (-> (wrap-defaults app-routes site-defaults)
      (wrap-json-body {:keywords? true})
      ))
