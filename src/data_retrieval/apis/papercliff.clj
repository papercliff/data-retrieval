(ns data-retrieval.apis.papercliff
  (:require [clj-http.client :as client]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [environ.core :as env]))

(defn call [endpoint params]
  (log/info "getting" endpoint params)
  (Thread/sleep 5000)
  (-> :papercliff-core-url
      env/env
      (str "/api/v1/" endpoint)
      (client/get
        {:content-type :json
         :headers      {(env/env :papercliff-core-header-name)
                        (env/env :papercliff-core-header-value)}
         :query-params params})
      :body
      (json/read-str :key-fn keyword)))
