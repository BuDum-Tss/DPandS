(ns task_6)

(def transaction-restarts (atom 0))
;;;an empty route map
;;;it is enough to use either forward or backward part (they correspond to each other including shared reference to number of tickets)
;;;:forward is a map with route start point names as keys and nested map as values
;;;each nested map has route end point names as keys and route descriptor as values
;;;each route descriptor is a map (structure in fact) of the fixed structure where
;;;:price contains ticket price
;;;and :tickets contains reference to tickets number
;;;:backward has the same structure but start and end points are reverted
(def empty-map
  {:forward  {},
   :backward {}})

(defn route
  "Add a new route (route) to the given route map
   route-map - route map to modify
   from - name (string) of the start point of the route
   to - name (string) of the end point of the route
   price - ticket price
   tickets-num - number of tickets available"
  [route-map from to price tickets-num]
  (let [tickets                (ref tickets-num :validator (fn [state] (>= state 0))),     ;reference for the number of tickets
        orig-source-desc       (or (get-in route-map [:forward from]) {}),
        orig-reverse-dest-desc (or (get-in route-map [:backward to]) {}),
        route-desc             {:price   price,
                                ;route descriptor
                                :tickets tickets},
        source-desc            (assoc orig-source-desc to route-desc),
        reverse-dest-desc      (assoc orig-reverse-dest-desc from route-desc)]
    (-> route-map
        (assoc-in [:forward from] source-desc)
        (assoc-in [:backward to] reverse-dest-desc))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(def inf 999999)

; default Dijkstra algorithm
(defn dijkstra [graph source destination]
  (loop [distances (assoc (zipmap (keys graph) (repeat inf)) source 0) ; Начальные расстояния от исходной точки
         current source
         adjacent (keys (get graph current)) ; Доступные смежные вершины
         unvisited (apply hash-set (keys graph)) ; Непосещенные вершины
         paths (zipmap (keys graph) (repeat nil))] ; Пути
    (if (empty? unvisited) ; Если все города посещены, найден кратчайший путь
      [(get distances destination), ; Возвращает длину кратчайшего пути
       (loop [path []
              previous destination]
         (if (= previous source)
           (cons source path) ; Формирует путь от исходной до конечной точки
           (recur (cons previous path) (get paths previous))))]
      (if (empty? adjacent)
        (let [unvisited (disj unvisited current)
              next (first (sort-by #(get distances %) unvisited))] ; Выбирает следующую ближайшую вершину
          (recur distances next (filter #(some (fn [s] (= s %)) unvisited) (keys (get graph next))) unvisited paths))
        (let [cdst (get distances current)  ; Вычисляет новое расстояние и обновляет расстояния
              idist (get distances (first adjacent))
              sum (+ cdst (get (get graph current) (first adjacent)))
              result (if (< sum idist)
                       (assoc distances (first adjacent) sum)
                       distances)
              path (if (< sum idist) ; Обновляет путь, если найден более короткий путь
                     (assoc paths (first adjacent) current)
                     paths)]
          (recur result current (rest adjacent) unvisited path))))))

(defn simplify-graph [graph]  ; Функция упрощает граф для поиска пути
  ;; Получаем часть графа :forward
  (let [simplified-graph (get graph :forward)]
    (zipmap
      ;; Для каждого города из :forward
      (keys simplified-graph)
      ;; Создаем новую структуру графа с ценой билетов или высоким значением, если билеты закончились
      (map #(zipmap
              (keys %)
              (map
                ;; Для каждого маршрута в графе, eсли есть билеты, используем цену билета, иначе устанавливаем высокую цену
                (fn [param1] (if
                               (> (deref (param1 :tickets)) 0)
                               (get param1 :price) ; Используем цену билета
                               9999)) ; Устанавливаем высокую цену, если билеты закончились
                (vals %))) ; Получаем значения маршрутов из :forward
           (vals simplified-graph)))))  ; Получаем значения из упрощенного графа :forward

;;implementation must be pure functional besides the transaction itself, tickets reference modification and
;;restarts monitoring (atom could be used for this)
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
(defn book-tickets
  "Tries to book tickets and decrement appropriate references in route-map atomically
   returns map with either :price (for the whole route) and :path (a list of destination names) keys
          or with :error key that indicates that booking is impossible due to lack of tickets"
  [route-map from to]
  (if (= from to)
    {:path '(), :price 0}
    (let [graph (simplify-graph route-map) ; Упрощаем граф для поиска маршрута
          path (dijkstra graph from to)] ; Находим кратчайший путь с помощью алгоритма Дейкстры
      (try
        (dosync ; Начинаем транзакцию
          (swap! transaction-restarts inc) ; Увеличиваем счетчик перезапусков транзакций
          (loop [p (second path)] ; Проходим по пути
            (if (< (count p) 3)
              nil ; Выходим из цикла
              (let [r (:tickets (get (get (:forward route-map) (first p)) (second p)))]
                (do
                  (alter r dec) ; Уменьшаем количество билетов, если они есть; если билетов нет, генерируется исключение
                  (recur (next p)))))) ; Переходим к следующему рейсу в пути
          (swap! transaction-restarts dec)) ; Уменьшаем счетчик перезапусков транзакций
        {:path (second path), :price (first path)} ; Возвращаем маршрут и его цену
        (catch Exception e {:error e}))))) ; Поймали исключение -> прерываем транзакцию и возвращаем ошибку

;;;cities
(def spec1
  (-> empty-map
      (route "City1" "Capital" 200 5)
      (route "a" "Capital" 200 5)
      (route "Capital" "City1" 250 5)
      (route "City2" "Capital" 200 5)
      (route "Capital" "City2" 250 5)
      (route "City3" "Capital" 300 3)
      (route "Capital" "City3" 400 3)
      (route "City1" "Town1_X" 50 2)
      (route "Town1_X" "City1" 150 2)
      (route "Town1_X" "TownX_2" 50 2)
      (route "TownX_2" "Town1_X" 150 2)
      (route "Town1_X" "TownX_2" 50 2)
      (route "TownX_2" "City2" 50 3)
      (route "City2" "TownX_2" 150 3)
      (route "City2" "Town2_3" 50 2)
      (route "Town2_3" "City2" 150 2)
      (route "Town2_3" "City3" 50 3)
      (route "City3" "Town2_3" 150 2)))

(defn booking-future [route-map from to init-delay loop-delay]
  (future
    (Thread/sleep init-delay)
    (loop [bookings []]
      (Thread/sleep loop-delay)
      (let [booking (book-tickets route-map from to)]
        (if (booking :error)
          bookings
          (recur (conj bookings booking)))))))

(defn print-bookings [name ft]
  (println (str name ":") (count ft) "bookings")
  (doseq [booking ft]
    (println "price:" (booking :price) "path:" (booking :path))))

(defn run []
  ;;try to tune timeouts in order to all the customers gain at least one booking
  (let [f1 (booking-future spec1 "City1" "City3" 0 1),
        f2 (booking-future spec1 "City1" "City2" 100 1),
        f3 (booking-future spec1 "City2" "City3" 100 1)]
    (print-bookings "City1->City3:" @f1)
    (print-bookings "City1->City2:" @f2)
    (print-bookings "City2->City3:" @f3)
    (println "Total restarts:" @transaction-restarts)
    ))

(defn -main []
  (run)
  (Thread/sleep 2000)
  (shutdown-agents))