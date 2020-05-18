(ns priority-chan.core
  (:require [clojure.core.async.impl.protocols :as impl]
            [clojure.core.async :as async :refer [chan go-loop <!]])
  (:import (clojure.core.async.impl.channels ManyToManyChannel)
           (clojure.lang Counted)
           (java.util AbstractQueue Iterator)
           (java.util.concurrent PriorityBlockingQueue)))

(defn priority-comparate [idfn]
 (fn [a b]
   (> (idfn a)
      (idfn b))))

(defn iteration-seq [iteration]
  (iterator-seq
   (reify Iterator
     (hasNext [this] (.hasNext iteration))
     (next [this] (.next iteration))
     (remove [this] (.remove iteration)))))

(deftype PriorityBuffer [^AbstractQueue buf ^ManyToManyChannel rchan ^long n]
  impl/Buffer
  (full? [this]
    (>= (.size buf) n))
  (remove! [this]
    (.poll buf))
  (add!* [this itm]
    (.add buf itm)
    this)
  (close-buf! [this]
    (async/close! rchan))
  Counted
  (count [this]
    (.size buf)))

(defn priority-buf [n pfn idfn rchan]
  (let [queue (PriorityBlockingQueue. (inc n) (comparator pfn))
        removals (atom #{})]
    (go-loop []
      (when-let [x (<! rchan)]
        (swap! removals conj (idfn x))
        (recur)))
    (go-loop []
      (<! (async/timeout 500))
      (when-not (empty? @removals)
        (let [elems (iteration-seq (.iterator queue))
              to-remove (filter #(contains? @removals (idfn %)) elems)]
          (doseq [e to-remove]
            (.remove queue e))))
      (when-not (.closed? rchan)
        (recur)))
    (PriorityBuffer. queue rchan n)))

(defn priority-chan [n idfn rchan]
  (chan (priority-buf n
                      (priority-comparate idfn)
                      idfn
                      rchan)))
