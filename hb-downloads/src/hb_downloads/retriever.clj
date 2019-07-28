(ns hb-downloads.retriever 
  (:require [amazonica.aws.s3 :refer :all]
    [hb-downloads.config :as config]
    [clj-fuzzy.metrics :as fuzz]
    [clj-time.core :as t]
    )
  (:import [java.util Date])
  )

(def date (/ 1000 (.getMillis (t/plus (t/now) (t/hours 24)))))

(defn gen-url
  "Generates AWS presigned URL with these credentials"
  [config s3key]
  (str (generate-presigned-url (:aws config) (:bucket config) s3key date)) )

(defn all-items
  [config]
  (let [creds (:aws config)
    bucket (:bucket config)
    prefix (:prefix config)]
    (map :key 
     (:object-summaries (list-objects creds :bucket-name bucket :prefix prefix)))))

(defn clean-name
  "Removes predictable junk strings from the s3 album name"
  [s3string]
  (second (re-find #".*\/(.*)\.\w+$" s3string)))


(defn get-dices 
  "Returns albums and their dice values"
  [testing-str r]
  (loop [results {}
   remainder r]
   (if (empty? remainder)
    results
    (recur (assoc results (first remainder) (fuzz/dice testing-str (first remainder)))
     (rest remainder)))))

(defn best-fuzzy
  "Get the closest-matching album"
  [test-string items]
  (apply max-key val (get-dices test-string items)))

(defn get-s3-key
  [config test-string]
  (let [items (all-items config)
    best-match (best-fuzzy test-string items)
    item-exists (> (second best-match) 0.3)]
    (if item-exists
      (key best-match)
      nil)))

(defn get-item
  "Checks S3 for a similar item; if we can't match anything, return null.
  Otherwise, return the true string."
  [config test-string]
  (let [key-match (get-s3-key config test-string)]
      (if key-match
        (clean-name key-match)
        nil)))

(defn download-map
  [conf item-title]
  (let [s3key (get-s3-key conf item-title)
        readable (clean-name s3key)
        url (gen-url conf s3key)]
    {:album s3key :url url :readable readable}))
