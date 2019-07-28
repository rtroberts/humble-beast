(ns hb-downloads.mailing-list
		(:require [clojchimp.client :as chmp]
            [hb-downloads.config :as config]
            ))

(defn make-client 
  [config]
  (chmp/create-client (:email 	(:mailing-list config))
                      (:api-key (:mailing-list config))))

(defn add-to-mailing-list
  "Adds given user to the proper mailing list (:id cf8bca7236)"
  ; TODO: The proper string should be "cf8bca7236". It is currently "s/cf8bca7236" to 
  ; support a bug in the clojchimp library. (which has been fixed upstream)
  [config params]
  (let [client (make-client config)
        list-id (str "s/" (:list-id (:mailing-list config)))]
    (chmp/create-member-for-list client list-id {:email_address (:email params)
                                                 :status "subscribed"
                                                 :merge_fields {:FNAME (:name params)
                                                                :LNAME ""
                                                                :NUMBERYUI (:zip_code params)
                                                                :NUMBERYUI2 (:zip_code params)
                                                                :MERGE3 (:zip_code params)
                                                                :MERGE5 (:zip_code params)}})))

;; Or merge3 and merge5?
