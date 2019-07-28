(ns hb-downloads.handler
  (:require [compojure.core :refer :all]
    [compojure.route :as route]
    [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
    [ring.middleware.json :refer [wrap-json-body]]
    [hb-downloads.views :as views]
    [hb-downloads.database :as database]
    [hb-downloads.config :as config]
    [hb-downloads.mailing-list :as mailing-list]
    [hb-downloads.url-handler :as url-handler]
    [hb-downloads.email-handler :as email-handler]
    [hb-downloads.retriever :as retriever]
    [ring.middleware.gzip :refer [wrap-gzip]]
    [ring.util.response :as resp]))


(def hb-home
  "http://www.humblebeast.com/")
(def streetlights-home
  "http://www.streetlights.com")

(def config-edn (config/readcfg))

(defn site-switch 
  "Return config based on the route and/or the querystring"
  [site]
  (cond (= site "hb") (:hb config-edn) 
    (= site "sl") (:sl config-edn)
    :else (:hb config-edn)))

(defroutes app-routes
  ; If you don't provide an album arg... to the brig with you
  (GET "/albums" [] (resp/redirect hb-home))
  (GET "/albums/" [] (resp/redirect hb-home))
  (GET "/streetlights" []  (resp/redirect streetlights-home))
  (GET "/streetlights/" [] (resp/redirect streetlights-home))
  (GET "/" [] (resp/redirect hb-home))
  
  ; Wraps the whole site. We'll use a route to decide what look & feel to use.
  (context "/:site" [site]

           ; Redirected from the Downloads page on the home site
           ; If that album exists, but the text isn't quite right, redirect
           ; to the proper page. otherwise, 404
           (GET "/albums/:item" [item] 
            (let 
                  ;; does the file exist? if so, return the full display-name
                  [conf (site-switch site)
                  display-name (retriever/get-item conf item)
                  url-friendly (url-handler/urlify display-name)]
                  (cond 
                    ; If the file exists, and the user is on the correct page
                    (and display-name url-friendly (= url-friendly item)) (views/add-downloader conf display-name url-friendly)
                    display-name (resp/redirect (str "/" site "/albums/" url-friendly))
                    :else (route/not-found (views/not-found conf))))) 
           
           (GET "/streetlights/:item" [item] 
            (let 
              [conf (site-switch site) 
              display-name (retriever/get-item conf item)
              url-friendly (url-handler/urlify display-name)]
              (cond 
                    ; If the file exists, and the user is on the correct page
                    (and display-name url-friendly (= url-friendly item)) (views/add-downloader conf display-name url-friendly)
                    display-name (resp/redirect (str "/" site "/streetlights/" url-friendly))
                    :else (route/not-found (views/not-found conf))))) 
           
           ; Form posts to here; generates DB entry
           (POST "/downloaders/create" json-params
             (let [conf (site-switch site) 
               body (:body json-params)
               params (assoc body :onetime_key (url-handler/generate site))
               result (future 
                (do (database/create params)
                                  ;; Should be using a PUT for mailchimp, but the lib doesn't support yet.
                                  ;; Currently it throws an exception (a 400 I think)
                    (try (mailing-list/add-to-mailing-list conf params)
                      (catch Exception e (println (str "caught exception: " (.getMessage e) e))))
                    (email-handler/send-email conf params)))]
                   ; TODO: We need a better logging solution than this.
                   ;(println @result)
                   (resp/redirect (str "/" site "/email"))))

           
           
           ; Redirect from downloader DB entry creation
           (GET "/email" [] 
            (let [conf (site-switch site)]
              (views/email-page conf)))
           
           ; This is the address they get in their email
           (GET "/download/:one-time" {body :body headers :headers params :params}
            (let [conf (site-switch site)]
                  ;; If we have a mobile browser, redirect and don't eat their key.
                  (if (seq (re-find #"Mobile|Android|BlackBerry" (get headers "user-agent")))
                    ;; To mobile "we don't support this" page
                    (views/mobile-page conf)
                    ; "Otherwise, let's serve it up."
                    (let [valid? (database/valid-key? (:one-time params))
                      db-album (database/get-album (:one-time params))]
                      (if valid?
                        (->> db-album
                          (retriever/download-map conf)
                          (views/thanks-page conf))
                        (views/already-retrieved conf site (str "Sorry, this key has been used or is invalid.")))))))
           
           ; Redirect after download starts
           (GET "/thanks/:album" [album] 
                ; Not quite sure what to do with this page if there isn't album info.
                (let [conf (site-switch site)]
                  (if (seq album)
                    (views/thanks-page conf album)
                    (resp/redirect "http://humblebeast.com"))))
           
           (ANY "*" request
            (let [conf (site-switch site)]
              (views/not-found conf))))

  ; Serve static resources 
  (route/resources "/"))

(def app
  (-> (wrap-defaults app-routes site-defaults)
    (wrap-json-body {:keywords? true})))
