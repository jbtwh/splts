(ns splts.taskone
  (:import
    (java.util.regex Pattern)
    (org.apache.http.impl.client HttpClients)
    (org.apache.http.client.methods HttpGet)
    (org.apache.http HttpEntity)
    (org.apache.http.util EntityUtils)
    (com.google.common.util.concurrent RateLimiter))
  (:require
    [clojure.reflect :as r]
    [clojure.string :as str]
    [clojure.java.io :as io]
    [splts.tasktwo :as two]
    [clojure.data.json :as json]))


(def httpclient (HttpClients/createDefault))

(def token "9815430f7607284682c2b03c34930f3514b9e928cd6a6ef9ec75d91b5e9cd24e")

(def followersurl (format "https://api.dribbble.com/v1/users/{name}/followers?access_token=%s&page={page}" token))

(def usershotssurl (format "https://api.dribbble.com/v1/users/{name}/shots?access_token=%s&page={page}" token))

(def shotlikesurl (format "https://api.dribbble.com/v1/shots/{name}/likes?access_token=%s&page={page}" token))

(def ratelimiter (RateLimiter/create 1.0))

(defn checkfornextpage
  [headers]
  (if (.contains (str/join (map #(.toString %) headers)) "rel=\"next\"") true false))

(defn call
  [url]
  (.acquire ratelimiter)
  (let [get (new HttpGet url)
        response (.execute httpclient get)
        entity (.getEntity response)
        headers (.getAllHeaders response)
        responseString (EntityUtils/toString entity "UTF-8")]
    (.getStatusLine response)
    (println url)
    (EntityUtils/consume entity)
    (.close response)
    {:headers headers
     :body responseString}))

(defn getinfo
  [url name]
  (loop [ins []
         page 1]
    (let [result (call (str/replace url #"\{name\}|\{page\}" {"{name}" (.toString name) "{page}" (.toString page)}))
          input (conj ins (:body result))]
      (if (checkfornextpage (:headers result))
        (recur input (inc page))
        (flatten (map json/read-str input))))))

(defn top10
  [name]
  (let [followers (getinfo followersurl name)
        followersnames (map #(get (get % "follower") "username") followers)
        shotsforuser (doall (flatten (map #(getinfo usershotssurl %) followersnames)))
        shotsforusernames (map #(get % "id") shotsforuser)
        likers (doall (flatten (map #(getinfo shotlikesurl %) shotsforusernames)))
        likersnames (map #(get (get % "user") "username") likers)]
    (take 10 (sort-by key > (group-by count likersnames)))))

;;(top10 "vertei")

