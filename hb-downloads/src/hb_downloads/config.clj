(ns hb-downloads.config
	(:require [clojure.edn :as edn]))

(defn readcfg
	"Read edn config file and return a config map"
	[]
	(edn/read-string (slurp "./src/config/config.edn")))