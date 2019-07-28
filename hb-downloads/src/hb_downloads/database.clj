(ns hb-downloads.database
  (:require [clojure.java.jdbc :as j]
            [hb-downloads.config :as config]
            ))

(use 'korma.core)
(use 'korma.db)


(defdb mysql-db (mysql (:database (:hb (config/readcfg)))))


(defn now []
  (str (java.sql.Timestamp. (System/currentTimeMillis))))

(defn get-by-key 
  "Gets yer info by onetime_key"
  [onetime_key]
  (do (defentity downloaders)
    (select downloaders
            (where {:onetime_key onetime_key}))))

(defn set-retrieved
  "Decrements the 'claimed' field in the database"
  [onetime_key]
  (do
    (defentity downloaders)
    ; I don't like querying twice here. 
    (let [current-count (:claimed (first (get-by-key onetime_key)))
          new-count (- current-count 1)]
      (update downloaders
              (set-fields {:claimed new-count})
              (where {:onetime_key onetime_key})))))

(defn get-album
  [onetime_key]
  (do (set-retrieved onetime_key)
    (:album (first (vec (get-by-key onetime_key))))))

(defn valid-key?
  "Does the given key get results, and are there downloads remaining"
  [onetime_key]
  (let [exists? (first (get-by-key onetime_key))]
    (and (seq exists?) (> (:claimed exists?) 0))))

(defn create 
  "Remove that nasty token and insert into DB"
  [params]
  (let [insertable (dissoc params :__anti-forgery-token)]
    (do 
      (println params)
      (defentity downloaders)
      (insert downloaders
              (values (merge insertable {:created_at (now)}
                                        {:claimed 3})))))) 
