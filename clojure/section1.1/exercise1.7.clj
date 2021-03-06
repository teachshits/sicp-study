;; (good-enough? 0.01 0.001)
;; -> true
;; .01 shouldn't be a good guess.
;; 
;; Large failures:
;; Potentially because squaring the guess, even when it is very close to the proper guess, will still cause it to be further than .001 away from goal number x.  This would cause infinite loop
;; user=> (good-enough? 31622.7766 1000000000)
;; false
;; 
;; Different implementation:

(defn change [first second]
 (Math/abs (- first second)))

(defn good-enough?? [first second]
 (< (change first second) (* 0.001 second)))

(defn average [x y]
 (/ (+ x y) 2))

(defn improve [guess x]
 (average guess (/ x guess)))

(defn sqrt-iter [prev-guess curr-guess x]
 (if (good-enough?? prev-guess curr-guess)
     curr-guess
     (recur curr-guess (improve curr-guess x) x)))

(defn sqrt [x]
 (sqrt-iter 0 1.0 x))

(sqrt 1000000000)
(sqrt 0.001)

;; Using it:
;; user=> (sqrt 1000000000)
;; 31622.780588899368
;; 
;; user=> (sqrt 0.001)
;; 0.03162278245070105
;; user=> (* 0.03162278245070105 0.03162278245070105)
;; 0.0010000003699243661



