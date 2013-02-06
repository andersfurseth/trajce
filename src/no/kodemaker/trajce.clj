(ns no.kodemaker.trajce
  (:use [clojure.walk :only [postwalk]]))

(defn remove-fn-and-splice [fn-name form]
  (if (and (list? form)
           (= fn-name (first form)))
    (apply concat (rest form))
    form))

(defmacro print-form [form]
  `(let [result# ~form
         clean-form-fn# (partial remove-fn-and-splice 'no.kodemaker.trajce/print-form)
         cleaned-form# (postwalk clean-form-fn# '~form)
         cleaned-result# (postwalk clean-form-fn# result#)]
     (println cleaned-form# "=>" cleaned-result#)
     result#))

(defn wrap-list-with-debug [form]
  (if (list? form)   ;; How can we detect quoted form?
    `(print-form ~form)
    form))

(defmacro debug [form]
  "Prints the form (and all nested forms) and their result to *stdout*."
  (postwalk wrap-list-with-debug form))

(comment
  (debug (+ (+ 1 2 (+ 10 20 (+ 30 40))) (+ 3 4)))

  (debug (let [a 1
               b 2]
           (* (inc a) (inc b))))

  ;; Fails, kinda. Result form this expansion still includes print-form
  ;;   (1 [1 2 [3 4 [5 (eventum-api.debug/print-form (+ 6 7))]]])
  ;; The printout is fine though, given cleaning of result, but we're actually
  ;; evaling the quoted form.
  (debug (list 1 [1 2 [3 4 [5 '(+ 6 7) (+ 8 9)]]]))
)
