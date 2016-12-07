(ns splts.tasktwo
  (:import
    (java.util.regex Pattern)
    )
  (:require
    [clojure.reflect :as r]
    [clojure.string :as str]
    [clojure.java.io :as io]))

(defn in?
  "true if coll contains elm"
  [coll elm]
  (some #(= elm %) coll))

(defn urlutil
  [pattern url]
  (let [groups (re-find #"(.+)\((.+)\)" pattern)
        part (get groups 1)
        value (get groups 2)]
    (case part
      "host" (if (= (.getHost url) value) (.getHost url) nil)
      "path" (let [path (str/replace-first (.getPath url) #"/" "")
                   splittedvalue (str/split value #"/")
                   replacedsplittedvalue (map #(if (.contains % "?") "(.+)" %) splittedvalue)
                   joinedreplacedsplittedvalue (str/join "/" replacedsplittedvalue)
                   result (re-find (re-pattern joinedreplacedsplittedvalue) path)
                   keys (zipmap (map (comp keyword #(str/replace-first % #"\?" "")) (filter #(.contains % "?") splittedvalue)) (drop 1 result))]
               ;;(println keys)
               (if (nil? result) nil keys))
      "queryparam" (let [queryparam (.getQuery url)
                         replacedvalue (str/replace value #"=(\?.+)" "=([^&]+)")
                         result (re-find (re-pattern replacedvalue) queryparam)
                         key (hash-map (keyword (get (re-find #"=\?(.+)" value) 1)) (get result 1))]
                     ;;(println value replacedvalue queryparam result)
                     (if (nil? result) nil key)))))

(defn recognize
  [pattern url]
  (let [splits (str/split pattern #";")
        result (map #(urlutil % url) splits)]
    ;;(println result)
    ;;(println splits)
    (if (in? result nil) nil (filter map? result))))


(def pattern1 "host(twitter.com);path(?user/status/?id)")

(def url1 (io/as-url "http://twitter.com/bradfitz/status/562360748727611392"))

(def pattern2 "host(dribbble.com);path(shots/?id);queryparam(offset=?offset)")

(def url2 (io/as-url "https://dribbble.com/shots/1905065-Travel-Icons-pack?list=users&offset=1"))

(def url3 (io/as-url "https://twitter.com/shots/1905065-Travel-Icons-pack?list=users&offset=1"))

(def url4 (io/as-url "https://dribbble.com/shots/1905065-Travel-Icons-pack?list=users"))

(def pattern3 "host(dribbble.com);path(shots/?id);queryparam(offset=?offset);queryparam(list=?type)")

(def url5 (io/as-url "https://dribbble.com/shots/1905065-Travel-Icons-pack?list=users&offset=11&param=0&param2=1&param3=333"))

(def pattern4 "host(dribbble.com);path(shots/?id);queryparam(offset=?offset);queryparam(param2=?param2);queryparam(param3=?param3)")

(recognize pattern1 url1)

(recognize pattern2 url2)

(recognize pattern2 url3)

(recognize pattern2 url4)

(recognize pattern3 url4)

(recognize pattern3 url5)

(recognize pattern4 url5)