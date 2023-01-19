(ns data-retrieval.apis.github
  (:require [clj-github.httpkit-client :as github-client]
            [clj-github.changeset :as github-change]
            [clj-http.client :as client]
            [clojure.data.json :as json]
            [environ.core :as env]
            [taoensso.timbre :as timbre]))

(def raw-github-prefix
  "https://raw.githubusercontent.com/papercliff/historical-data/master/")

(defn- github-response [path]
  (-> raw-github-prefix
      (str path)
      (client/get {:throw-exceptions false})))

(defn file-exists? [path]
  (-> path
      github-response
      :status
      (= 200)))

(defn load-content [path]
  (timbre/info "loading github contents from" path)
  (-> path
      github-response
      :body
      (json/read-str :key-fn keyword)))

(defn save-content
  [path content]
  (timbre/infof "saving github contents to %s and wait" path)
  (let [client (github-client/new-client
                 {:token (env/env :github-token)})]
    (-> (github-change/from-branch!
          client
          "papercliff"
          "historical-data"
          "master")
        (github-change/put-content
          path
          (json/write-str
            content
            :indent true))
        (github-change/commit!
          (str "Auto file addition " path))
        (github-change/update-branch!)))
  (Thread/sleep 60000))
