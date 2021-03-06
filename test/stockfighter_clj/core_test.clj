(ns stockfighter-clj.core-test
  (:require [clojure.test :refer :all]
            [stockfighter-clj.core :refer :all]))

(def q
  (comp get-body query))

(def o
  (comp get-body order))

(defn map->keySet [m]
  (into #{} (keys m)))

(defn keys-present [m k]
  (= (map->keySet m) k))

(def test-order
  {:action :place :account "EXB123456" :venue "TESTEX" :stock "FOOBAR" :price 10000 :qty 100 :direction "buy" :orderType "fill-or-kill"})

(def order-res-keys
  #{:ok :symbol :venue :direction :originalQty :qty :price :orderType :id :account :ts :fills :totalFilled :open})


(deftest query-test
  (testing "Can check api status"
    (is (keys-present (q {:tag :heartbeat}) #{:ok :error})))
  (testing "Can check venue status"
    (is (keys-present (q {:tag :heartbeat :venue "TESTEX"}) #{:ok :venue})))
  (testing "Can get socks on a venue"
    (is (keys-present (q {:tag :stocks :venue "TESTEX"}) #{:ok :symbols})))
  (testing "Can get orderbook for a stock"
    (is (keys-present (q {:tag :orderbook :venue "TESTEX" :stock "FOOBAR"}) #{:ok :venue :symbol :ts :bids :asks})))
  (testing "Can get a quote for a stock"
    (is (keys-present (q {:tag :quote :venue "TESTEX" :stock "FOOBAR"}) #{:ok :symbol :venue :bidSize :askSize :bidDepth :askDepth :last :lastSize :lastTrade :quoteTime}))))

(deftest order-test
  (testing "can place order"
    (is (keys-present (o test-order)
                      order-res-keys)))
  (testing "can cancel order"
    (let [id (:id (o (assoc test-order :action :place)))]
      (is (keys-present (o {:action :cancel :id id :venue "TESTEX" :stock "FOOBAR"})
                        order-res-keys))))
  (testing "can check order status"
    (let [id (:id (o test-order))]
      (is (keys-present (o {:action :status :id id :venue "TESTEX" :stock "FOOBAR"})
                        order-res-keys))))
  (testing "can get status for all orders for an account on a given venue"
    (is (keys-present (o {:action :all-orders :venue "TESTEX" :account "EXB123456"})
                      #{:ok :venue :orders})))
  (testing "can get status of all orders for a given stock and account on a given venue"
    (is (keys-present (o {:action :stock-orders :venue "TESTEX" :account "EXB123456" :stock "FOOBAR"})
                      #{:ok :venue :orders}))))
