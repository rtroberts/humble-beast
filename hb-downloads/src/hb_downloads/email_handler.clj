(ns hb-downloads.email-handler
  (:require [hb-downloads.url-handler :as url-handler]
    [postal.core :as email]
    [amazonica.aws.simpleemail :as ses]
    [hb-downloads.config :as config]
    ))

(def html-text
  "<p>Thank you for visiting us at ORG_NAME and downloading our free resources. <a href='DOWNLOAD_LINK'>Here's your link.</a>
  Make sure to download on a desktop browser or your download will not work!</p>
  
  <p>We’re grateful for your support and there are many ways you can continue to fuel our work here at ORG_NAME.</p>
  
  <p>Purchase something from our <a href='STORE'>online store.</a><br> 
  Give generously either <a href='DONATE'>one time or monthly.</a></p> 
  
  <p>Your continued support and generosity is vital to us, we can’t do it without you.</p>
  
  <p>Keep up with us at:<br>
  <a href='FACEBOOK'>Facebook</a><br>
  <a href='TWITTER'>Twitter</a><br>
  <a href='INSTAGRAM'>Instagram</a><br>
  <a href='YOUTUBE'>Youtube</a><br>
  SNAPCHAT
  </p>")

(def plain-text
  "Thank you for visiting us at ORG_NAME and downloading our free resources. Here’s your link:
  DOWNLOAD_LINK   
  Make sure to download on a desktop browser or your download will not work!
  
  We’re grateful for your support and there are many ways you can continue to fuel our work here at ORG_NAME.
  
  Purchase something from our online store: STORE 
  Give generously either one time or monthly: DONATE
  
  Your continued support and generosity is vital to us, we can’t do it without you.
  
  Keep up with us at:
  Facebook: FACEBOOK
  Twitter: TWITTER
  Instagram: INSTAGRAM
  YouTube: YOUTUBE
  SNAPCHAT")

(defn build-email
  "Makes the appropriate email string"
  [e-conf params text]
  (let [download-link (str (:org-site e-conf) (:org-prefix e-conf) "/download/" (:onetime_key params))]
    (-> text
      (clojure.string/replace "DOWNLOAD_LINK" download-link)
      (clojure.string/replace "ORG_NAME" (:org-name e-conf))
      (clojure.string/replace "STORE" (:store e-conf))
      (clojure.string/replace "DONATE" (:donate e-conf))
      (clojure.string/replace "FACEBOOK" (:facebook e-conf))
      (clojure.string/replace "TWITTER" (:twitter e-conf))
      (clojure.string/replace "INSTAGRAM" (:instagram e-conf))
      (clojure.string/replace "YOUTUBE" (:youtube e-conf))
      (clojure.string/replace "SNAPCHAT" (:snapchat e-conf)))))


(defn send-email
  "Sends an email to the proper person with a one-time use URL"
  [conf params]
  (let [e-conf (:email conf)
        sesconf {:access-key (:access-key (:aws conf))
                 :secret-key (:secret-key (:aws conf))
                 :endpoint   (:endpoint e-conf)}]
    (ses/send-email sesconf
      :destination {:to-addresses [(:email params)]}
      :source (:source-address e-conf)
      :message {:subject (str "[" (:org-name e-conf) "] Your download link")
      :body {:html (build-email e-conf params html-text)
       :text (build-email e-conf params plain-text)}})))
