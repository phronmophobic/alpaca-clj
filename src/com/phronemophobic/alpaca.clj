(ns com.phronemophobic.alpaca
  (:require [clj-http.client :as client]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.data.json :as json]
            [clojure.java.io :as io]))

(def secrets
  (with-open [rdr (io/reader "secrets.edn")
              rdr (java.io.PushbackReader. rdr)]
    (edn/read rdr)))

(def endpoint-base (-> secrets :alpaca :endpoint))
(defn endpoint [suffix]
  (str endpoint-base suffix))

(defn with-auth [req]
  (assoc req
         :headers {"APCA-API-KEY-ID" (-> secrets
                                         :alpaca
                                         :key)
                   "APCA-API-SECRET-KEY" (-> secrets
                                             :alpaca
                                             :secret)}))
(defn account []
  (client/get (endpoint "/v2/account")
              (with-auth {})))


(defn latest-stock-quote [symbols]
  (:body
   (client/get "https://data.alpaca.markets/v2/stocks/quotes/latest"
               (with-auth
                 {:query-params {:symbols
                                 (str/join
                                  ","
                                  symbols)
                                 :feed "iex"}
                  :as :json
                  :accept :json}))))

(defn create-order [{:keys [symbol notional]
                     :as order}]
  (:body
   (client/post (endpoint "/v2/orders")
                (with-auth
                  {:content-type :json
                   :form-params
                   (merge
                    {:side "buy"
                     :type "market"
                     :time_in_force "day"}
                    order)
                   :accept :json
                   :as :json}))))

(comment

  (create-order {:symbol "VOO"
                 :notional "1.22"})

  (create-order {:symbol "TSLA"
                 :notional "49.45"
                 :side "sell"})

  ,)


(defn list-orders []
  (client/get (endpoint "/v2/orders")
              (with-auth
                {:query-params {:status "open"}
                 :as :json
                 :accept :json})))

(defn list-positions []
  (:body
   (client/get (endpoint "/v2/positions")
               (with-auth
                 {:accept :json
                  :as :json}))))


