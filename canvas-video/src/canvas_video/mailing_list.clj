(ns canvas-video.mailing-list
		(:require [clojchimp.client :as chmp]
	            [canvas-video.config :as config]
			))

(def client (chmp/create-client (:email 	(:mailing-list (config/readcfg)))
															 	(:api-key (:mailing-list (config/readcfg)))))


(defn add-to-mailing-list
	"Adds given user to the proper mailing list (:id cf8bca7236)"
	; TODO: The proper string should be "cf8bca7236". It is currently "s/cf8bca7236" to 
  ; support a bug in the clojchimp library. (which has been fixed upstream)
	[params]
	(chmp/create-member-for-list client "s/cf8bca7236" {:email_address (:email params)
                                                    :status "subscribed"
                                                    :merge_fields {:FNAME (:name params)
                                                                   :LNAME ""
                                                                   :NUMBERYUI (:zip_code params)
                                                                   :NUMBERYUI2 (:zip_code params)
                                                                   :MERGE3 (:zip_code params)
                                                                   :MERGE5 (:zip_code params)}}))

;; Or merge3 and merge5?
