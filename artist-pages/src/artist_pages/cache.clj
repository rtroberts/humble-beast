(ns artist-pages.cache
  (:require [taoensso.carmine :as car :refer (wcar)]))

; Take the defaults (including local redis, local pool settings, etc)
(def srv-conn {})
(defmacro wcar* [& body] `(car/wcar srv-conn ~@body))

(def expiration 
  1800)

(defn grab
  "Grab the key from redis"
  [ourkey]
  (wcar*
    (car/get ourkey)))

(defn store 
  "Store the key and set the expiration"
  [storekey value]
  (wcar* 
    (car/set storekey value)
    (car/expire storekey expiration)))
