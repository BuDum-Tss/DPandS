(ns test
  (:require [clojure.test :refer :all])
  (:use task_3))

(defn heavy-even? [n]
  (Thread/sleep 10)
  (even? n))

(deftest primes-test
  (is (= (filter odd? (range 0)) (primes odd? (range 0))))
  (is (= (filter even? (range 100)) (primes even? (range 100))))
  (is (= (filter even? (range 1000)) (primes even? (range 1000)))))

(deftest primes-time-test
  (time (doall (take 100 (filter heavy-even? (iterate inc 0)))))
  (time (doall (take 100 (primes heavy-even? (iterate inc 0))))))(ns test)
