(ns data-retrieval.ut.call
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [environ.core :as env]))

(defn call [endpoint params]
  (-> (env/env :papercliff-base-url)
      (str endpoint)
      (client/get
        {:content-type :json
         :headers      {"x-rapidapi-proxy-secret"
                        (env/env :x-rapidapi-proxy-secret)}
         :query-params params})
      :body
      (json/read-str :key-fn keyword)))
