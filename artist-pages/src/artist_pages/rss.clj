(ns artist-pages.rss
  (:require [feedparser-clj.core :as feedparser]
            [artist-pages.cache :as cache]
            ))


(def cachekey "rss:humblebeast")
(def article-count 3)

(defn get-new
  "Get the results of the feed"
  [url]
  (let [feed (feedparser/parse-feed url)]
    (spit "feed_text.txt" (pr-str feed))
    feed))

(defn refresh-cache
  "Take a URL and key to store and refreshes the cache"
  [url cache-key]
  (let [new-data (get-new url)
        our-key cache-key]
    ; Store the new data
    (cache/store our-key new-data)
    (println (str "refreshed cache: " new-data))
    ; Return the new data
    new-data
    ))

(defn read-feed
  "Parses given artist map and returns the feed data"
  [artist-map]
  ; TODO: Change global rss cache key to artist-specific
  (let [ourkey cachekey
        cache-results (cache/grab ourkey)]
    ; If we get items from the cache, take the top three results. Otherwise refresh the cache
    ; and grab the top three results.
    (if (seq (:entries cache-results))
      (take article-count (:entries cache-results))
      (take article-count (:entries (refresh-cache (:rss artist-map) cachekey))))))
