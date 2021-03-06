(ns priority-chan.core
  (:require [clojure.core.async.impl.protocols :as impl]
            [clojure.core.async :as async :refer [chan go-loop <!]])
  (:import (clojure.core.async.impl.channels ManyToManyChannel)
           (clojure.lang Counted)
           (java.util AbstractQueue Iterator)
           (java.util.concurrent PriorityBlockingQueue)))

(defn priority-comparate [pfn]
 (fn [a b]
   (> (pfn a)
      (pfn b))))

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

(defn priority-buf [n pfn idfn rtime rchan]
  (let [queue (PriorityBlockingQueue. (inc n) (comparator pfn))
        removals (atom #{})]
    (go-loop []
      (when-let [id (<! rchan)]
        (swap! removals conj id)
        (recur)))
    (go-loop []
      (<! (async/timeout rtime))
      (when-not (empty? @removals)
        (let [elems (iteration-seq (.iterator queue))
              to-remove (filter #(contains? @removals (idfn %)) elems)]
          (doseq [e to-remove]
            (.remove queue e))))
      (when-not (.closed? rchan)
        (recur)))
    (PriorityBuffer. queue rchan n)))

(defn priority-chan [n pfn idfn rtime rchan]
  (chan (priority-buf n
                      (priority-comparate pfn)
                      idfn
                      rtime
                      rchan)))
