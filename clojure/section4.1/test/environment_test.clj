
(ns environment-test
  (:use clojure.test
        environment))

(deftest can-create-frame
  (let [f (make-frame '(a b c d) '(1 2 3 4))]
    (is (= '(a b c d) (sort (frame-variables f))))
    (is (= '(1 2 3 4) (sort (frame-values f))))))

(deftest can-add-vars-to-frame
  (let [f (make-frame '() '())]
    (add-binding-to-frame! 'a 1 f)
    (is (= '(a) (frame-variables f)))
    (is (= '(1) (frame-values f)))))

(deftest can-lookup-variable-in-environment
  (let [e (extend-environment '(a b c d)
                              '(1 2 3 4)
                              the-empty-environment)]
    (is (= 1 (lookup-variable-value 'a e)))))

(deftest can-set-variable-in-environment
  (let [e (extend-environment '(a b c d)
                              '(1 2 3 4)
                              the-empty-environment)]
    (set-variable-value! 'a 10 e)
    (is (= 10 (lookup-variable-value 'a e)))))

(deftest can-define-new-variable
  (let [e (extend-environment '(a b c d)
                              '(1 2 3 4)
                              the-empty-environment)]
    (define-variable! 'e 5 e)
    (is (= 5 (lookup-variable-value 'e e)))))

(deftest can-define-variable-which-already-exists
  (let [e (extend-environment '(a b c d)
                              '(1 2 3 4)
                              the-empty-environment)]
    (define-variable! 'b 11 e)
    (is (= 11 (lookup-variable-value 'b e)))))
