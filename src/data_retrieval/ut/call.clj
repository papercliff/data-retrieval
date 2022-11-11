(ns data-retrieval.ut.call
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [environ.core :as env]))

(defn call [endpoint params]
  (Thread/sleep 1000)
  (-> "https://papercliff.p.rapidapi.com/"
      (str endpoint)
      (client/get
        {:content-type :json
         :headers      {"X-RapidAPI-Key"
                        (env/env :x-rapidapi-key)

                        "X-RapidAPI-Host"
                        "papercliff.p.rapidapi.com"}
         :query-params params})
      :body
      (json/read-str :key-fn keyword)))
