(ns canvas-video.database
  (:require [clojure.java.jdbc :as j]
            [canvas-video.config :as config]
            ))

(use 'korma.core)
(use 'korma.db)


(defdb mysql-db (mysql (:database (config/readcfg))))

(defn all []
  (do (defentity downloaders)
      (select downloaders)))

	
(defn now []
	(str (java.sql.Timestamp. (System/currentTimeMillis))))


(defn create 
  "Remove that nasty token and insert into DB"
  [params]
  (let [insertable (dissoc params :__anti-forgery-token)]
    (do 
      (println params)
      (defentity downloaders)
      (insert downloaders
        (values (merge insertable {:created_at (now)})))))) 
