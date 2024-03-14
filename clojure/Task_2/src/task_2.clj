(ns task_2)

;; Переменная primes представляет собой бесконечную последовательность простых чисел.
(defn sieve [numbers]
  (let [p (first numbers)] ;; Берем первое число в последовательности
    (lazy-seq
      (cons p ;; Включаем текущее простое число в бесконечную последовательность
            (sieve (filter #(not= 0 (mod % p)) (rest numbers))))))) ;; Фильтруем оставшиеся числа

(def primes (sieve (iterate inc 2)))