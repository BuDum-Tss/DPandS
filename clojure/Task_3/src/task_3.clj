(ns task_3)

(defn primes
  ([pred coll]
   ;; Вычисляем количество доступных процессоров.
   (let [n (. (Runtime/getRuntime) availableProcessors)
         ;; Размер части для деления коллекции.
         chunk-size 20
         ;; Разбиваем коллекцию на части.
         parts (map doall (partition-all chunk-size coll))
         ;; Создаем функцию, которая будет вычислять простые числа в каждой части параллельно.
         primes #(future (doall (filter pred %)))
         ;; Создаем пул потоков для вычисления простых чисел в каждой части.
         pool (map primes parts)
         ;; Объединяет результаты вычислений из каждой части в единую последовательность.
         step (fn step [vs fs]
                ;; Если есть еще futures для вычисления:
                (if-let [s (seq fs)]
                  ;; Создаем ленивую последовательность результатов из каждого future.
                  (lazy-seq (lazy-cat (deref (first vs)) (step (rest vs) (rest s))))
                  ;; Если futures больше нет, объединяем все результаты в одну последовательность.
                  (apply concat (map deref vs))))]
     ;; Запускаем параллельные вычисления простых чисел в частях коллекции и объединяем результаты.
     (step pool (drop n pool)))))