(ns stockfighter-clj.core
  (:require [clojure.core.async :refer [go <! >! <!!]]
            [qbits.jet.client.http :as http]
            [qbits.jet.client.websocket :as ws]
            [environ.core :refer [env]]
            [cheshire.core :as json]))

(def client (http/client))

(def root-url "https://api.stockfighter.io/ob/api/")

(defn segment [params]
  (apply str (interpose "/" params)))

(defn build-url [params]
  (apply str root-url (segment params)))

(defn get-body
  [res]
  (-> res
       <!!
       :body
       <!!
       (json/parse-string true)))

(defmulti query (fn [params] (:tag params)))

(defmethod query :heartbeat
  [{:keys [venue]}]
  (if ((comp not nil?) venue)
    (http/get client (build-url ["venues" venue "heartbeat"]))
    (http/get client (build-url ["heartbeat"]))))

(defmethod query :stocks
  [{:keys [venue]}]
  (http/get client (build-url ["venues" venue "stocks"])))

(defmethod query :orderbook
  [{:keys [venue stock]}]
  (http/get client (build-url ["venues" venue "stocks" stock])))

(defmethod query :quote
  [{:keys [venue stock]}]
  (http/get client (build-url ["venues" venue "stocks" stock "quote"])))

(defmulti order (fn [params] (:action params)))

(defmethod order :place
  [{:keys [account venue stock price qty direction orderType]}]
  (http/post client (build-url ["venues" venue "stocks" stock "orders"])
             {:headers {"X-Starfighter-Authorization" (env :api-key)}
              :body (json/encode {"account" account
                                  "venue" venue
                                  "stock" stock
                                  "price" price
                                  "qty" qty
                                  "direction" direction
                                  "orderType" orderType})}))

(defmethod order :cancel
  [{:keys [id venue stock]}]
  (http/delete client (build-url ["venues" venue "stocks" stock "orders" id])
                      {:headers {"X-Starfighter-Authorization" (env :api-key)}}))

(defmethod order :status
  [{:keys [id venue stock]}]
  (http/get client (build-url ["venues" venue "stocks" stock "orders" id])
                   {:headers {"X-Starfighter-Authorization" (env :api-key)}}))

(defmethod order :all-orders
  [{:keys [venue account]}]
  (http/get client (build-url ["venues" venue "accounts" account "orders"])
                   {:headers {"X-Starfighter-Authorization" (env :api-key)}}))

(defmethod order :stock-orders
  [{:keys [venue account stock]}]
  (http/get client (build-url ["venues" venue "accounts" account "stocks" stock "orders"])
                   {:headers {"X-Starfighter-Authorization" (env :api-key)}}))
