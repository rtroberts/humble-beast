(ns hb-downloads.url-handler
  (:require [org.clojars.tnoda.simple-crypto :as c]
            [crypto.random :as r]
            ))

(defn urlify
  [string]
  (if string
    (-> (clojure.string/lower-case string)
        (clojure.string/replace #"[-']" "")
        (clojure.string/replace #"\s+" "-")
        (clojure.string/replace "." "-")
        ) 
    nil))

(defn generate
  "Generate one-time use URL"
  [prefix]
  (str prefix ":" (crypto.random/url-part 48)))
