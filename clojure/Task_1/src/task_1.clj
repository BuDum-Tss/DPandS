(ns task_1)

;; Проверяет, что в слове нет одинаковых символов, стоящих вместе.
(defn is-correct-word [word]
  (not= (get word (- (count word) 1)) (get word (- (count word) 2))))

;; Принимает список слов words и букву letter.
;; Возвращает список, где к каждому слову добавлена переданная буква.
(defn concat-letter [words letter]+
  (map (fn [word] (str word letter)) words))

;; Расширяет список слов words.
;; Добавляет к каждому слову из списка все возможные буквы алфавита, слова на корректность.
(defn extend-words [words alphabet]
  (filter
    is-correct-word
    (reduce
      (fn [extended-words letter] (concat extended-words (concat-letter words letter)))
      '()
      alphabet)))

;; Генерирует язык длины length из символов алфавита alphabet,
;; используя extend-words и повторяя этот процесс для каждой длины слова.
(defn create-language [length alphabet]
  (if (> length 0)
    (reduce
      (fn [words, _] (extend-words words alphabet)) alphabet (range (- length 1)))))

;; Проверка
(println (create-language 3 "abс"))
