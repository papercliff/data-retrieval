(ns data-retrieval.apis.papercliff
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [environ.core :as env]
            [taoensso.timbre :as timbre]))

(defn call [endpoint params]
  (timbre/info "getting" endpoint params)
  (Thread/sleep 5000)
  (-> "https://papercliff.p.rapidapi.com/"
      (str endpoint)
      (client/get
        {:content-type :json
         :headers {"X-RapidAPI-Key"
                   (env/env :x-rapidapi-key)

                   "X-RapidAPI-Host"
                   "papercliff.p.rapidapi.com"}
         :query-params params})
      :body
      (json/read-str :key-fn keyword)))
