(ns test
  (:require [clojure.test :refer :all])
  (:use task_2))

(deftest test-primes
  (let [first-10-primes (take 10 primes)]
    (is (= [2 3 5 7 11 13 17 19 23 29] first-10-primes))))