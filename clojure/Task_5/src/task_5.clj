(ns task_5)
(def philosophers-number 5) ; Количество философов
(def thinking-length 1000)  ; Продолжительность периода размышлений
(def dining-length 1000)    ; Продолжительность периода еды
(def periods-number 10)     ; Количество периодов

(def forks (take philosophers-number (repeatedly #(ref 0)))) ; Вилки, используемые философами
(def transaction-restarts (atom 0 :validator #(>= % 0)))    ; Для подсчета перезапусков транзакций

(defn philosopher [number left-fork right-fork]
  ;; Логика действий философа в течение нескольких периодов
  (dotimes [_ periods-number]
    (do
      (println (str "The philosopher " number " is thinking"))
      (Thread/sleep thinking-length) ; Философ размышляет в течение определенного времени
      (dosync
        (swap! transaction-restarts inc) ; Увеличиваем счетчик перезапусков транзакций
        (alter left-fork inc) ; Поднимаем левую вилку
        (println (str "The philosopher " number " picked up the left fork"))
        (alter right-fork inc) ; Поднимаем правую вилку
        (println (str "The philosopher " number " picked up the right fork"))
        (Thread/sleep dining-length) ; Обедать в течение определенного времени
        (println (str "The philosopher " number " finished the meal")) ; Философ завершил еду
        (swap! transaction-restarts dec))))) ; Уменьшаем счетчик перезапусков транзакций

(def philosophers (map #(new Thread (fn [] (philosopher % (nth forks %) (nth forks (mod (inc %) philosophers-number))))) (range philosophers-number)))

(defn dining-philosophers []
  ;; Запуск
  (do
    (doall (map #(.start %) philosophers)) ; Запускаем потоки для каждого философа
    (doall (map #(.join %) philosophers)))) ; Ожидаем завершения всех потоков философов

(time (dining-philosophers))
(println "Transaction restarts: " @transaction-restarts)
