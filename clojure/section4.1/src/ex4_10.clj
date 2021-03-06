; Exercise 4.10

; the functions definition-value and definition-variable have been changed so that
; defining functions is done similar to how it is done in clojure. Instead of
; the function name and parameters being in a single list, the function name
; is a located before a list of the parameters.
; (define fn-name (params) (body))

; Changing the syntax is quite simple.


(ns ex4_10
  (:use scheme-helpers
        environment))

(declare execute-application
         primitive-procedure-names
         primitive-procedure-objects)

(declare my-eval
         my-apply
         analyze)

(declare no-operands?
         first-operand
         rest-operands)

; Exercise 4.1
(defn list-of-values [exps env]
  (if (no-operands? exps)
    '()
    (let [left (my-eval (first-operand exps) env)
          right (list-of-values (rest-operands exps) env)]
      (cons left right))))
; Above function imposes a left to right ordering. If the
; assignments inside of let where switched it would be right
; to left

(declare if-predicate if-alternative if-consequent)

(defn eval-if [exp env]
  (if (my-eval (if-predicate exp) env)
    (my-eval (if-consequent exp) env)
    (my-eval (if-alternative exp) env)))

(declare last-exp? first-exp rest-exps)

(defn eval-sequence [exps env]
  (cond (last-exp? exps) (my-eval (first-exp exps) env)
        :else (do (my-eval (first-exp exps) env)
                  (eval-sequence (rest-exps exps) env))))

(declare assignment-variable assignment-value)

(defn eval-assignment [exp env]
  (set-variable-value! (assignment-variable exp)
                       (my-eval (assignment-value exp) env)
                       env)
  'ok)

(declare definition-variable definition-value)

(defn eval-definition [exp env]
  (define-variable!
    (definition-variable exp)
    (my-eval (definition-value exp) env)
    env)
  'ok)

(defn self-evaluating? [exp]
  (or (number? exp)
      (string? exp)
      (and (seq? exp) (self-evaluating? (first exp)))))

(defn variable? [exp]
  (or (symbol? exp)
      (= 'true exp)
      (= 'false exp)))

(defn tagged-list? [exp tag]
  (if (seq? exp)
    (= (first exp) tag)
    false))

(defn quoted? [exp]
  (tagged-list? exp 'quote))

(defn text-of-quotation [exp] (cadr exp))

(defn assignment? [exp]
  (tagged-list? exp 'set!))

(defn assignment-variable [exp] (second exp))

(defn assignment-value [exp] (nth exp 2))

(defn definition? [exp]
  (tagged-list? exp 'define))

; Changed this for 4.10
(defn definition-variable [exp]
  (second exp))

(declare make-lambda)

; Changed this for 4.10
(defn definition-value [exp]
  (if (= 3 (count exp))
    (nth exp 2)
    (make-lambda (nth exp 2)
                 (rest (rest (rest exp))))))


(defn lambda? [exp] (tagged-list? exp 'lambda))

(defn lambda-parameters [exp] (second exp))

(defn lambda-body [exp] (rest (rest exp)))

(defn make-lambda [parameters body]
  (cons 'lambda (cons parameters body)))

(defn if? [exp] (tagged-list? exp 'if))

(defn if-predicate [exp] (cadr exp))

(defn if-consequent [exp] (caddr exp))

(defn if-alternative [exp]
  (if (not (nil? (cdddr exp)))
    (cadddr exp)
    'false))

(defn make-if [predicate consequent alternative]
  (list 'if predicate consequent alternative))

(defn begin? [exp] (tagged-list? exp 'begin))

(defn begin-actions [exp] (cdr exp))

(defn last-exp? [xs] (null? (cdr xs)))

(defn first-exp [xs] (car xs))

(defn rest-exps [xs] (cdr xs))

(defn make-begin [xs] (cons 'begin xs))

(defn sequence->exp [xs]
  (cond (null? xs) xs
        (last-exp? xs) (first-exp xs)
        :else (make-begin xs)))

(defn pair? [x] (seq? x))

(defn application? [exp] (pair? exp))

(defn operator [exp] (car exp))

(defn operands [exp] (cdr exp))

(defn no-operands? [ops] (null? ops))

(defn first-operand [ops] (car ops))

(defn rest-operands [ops] (cdr ops))

(declare expand-clauses)
(defn cond? [exp] (tagged-list? exp 'cond))

(defn cond-clauses [exp] (cdr exp))

(defn cond-predicate [clause] (car clause))

(defn cond-else-clause? [clause]
  (= (cond-predicate clause) 'else))

(defn cond-actions [clause] (cdr clause))

(defn cond->if [exp]
  (expand-clauses (cond-clauses exp)))

(defn extended-cond? [clause]
  (and (list? clause)
       (> (count clause) 2)
       (= (second clause) '=>)))

(defn extended-cond-test [clause]
  (first clause))

(defn extended-cond-recipient [clause]
  (nth clause 2))

(defn expand-clauses [clauses]
  (if (null? clauses)
    'false
    (let [first-clause (car clauses)
          rest-clauses (cdr clauses)]
      (cond (cond-else-clause? first-clause)
            (if (null? rest-clauses)
              (sequence->exp (cond-actions first-clause))
              (Error. (str  "ELSE clause isn't last -- COND->IF"
                            clauses)))
            (extended-cond? first-clause)
            (make-if (extended-cond-test first-clause)
                     (list
                      (extended-cond-recipient first-clause)
                      (extended-cond-test first-clause))
                     (expand-clauses rest-clauses))
            :else
            (make-if (cond-predicate first-clause)
                     (sequence->exp (cond-actions first-clause))
                     (expand-clauses rest-clauses))))))

(defn make-procedure [parameters body env]
  (list 'procedure parameters body env))

(defn compound-procedure? [p]
  (tagged-list? p 'procedure))

(defn procedure-parameters [p] (cadr p))

(defn procedure-body [p] (caddr p))

(defn procedure-environment [p] (cadddr p))

(defn let? [exp]
  (tagged-list? exp 'let))

(defn named-let? [exp]
  (symbol? (second exp)))

(defn let-body [exp]
  (if (named-let? exp)
    (nth exp 3)
    (nth exp 2)))

(defn let-variables [exp]
  (if (named-let? exp)
    (map first (nth exp 2))
    (map first (second exp))))

(defn let-values [exp]
  (if (named-let? exp)
    (map second (nth exp 2))
    (map second (second exp))))

(defn let-name [exp]
  (second exp))

(defn make-definition [fn-name parameters body]
  (let [a 
        (list 'define (cons fn-name parameters) body)]
    (println a)
    a))

; define function
; eval function with arguments
(defn let->combination [exp]
  (let [parameters (let-variables exp)
        args (let-values exp)
        body (let-body exp)]
    (if (named-let? exp)
      (sequence->exp
       (list
        (make-definition (let-name exp)
                         parameters
                         body)
        (cons
         (let-name exp)
         args)))
      (cons
       (make-lambda (let-variables exp)
                    (list (let-body exp)))
       (let-values exp)))
    ))

(defn let*? [exp]
  (tagged-list? exp 'let*))

(defn make-let [clauses body]
  (list 'let (list clauses) body))

(defn let*->nested-lets [exp]
  (let [let-clauses (reverse (second exp))
        body (let-body exp)]
    (reduce #(make-let %2 %1) body let-clauses)))



(defn analyze-sequence [exps]
  (letfn [(sequentially [proc1 proc2]
                        (fn [env] (proc1 env) (proc2 env)))
          (lop [first-proc rest-procs]
               (if (null? rest-procs)
                 first-proc
                 (lop (sequentially first-proc (car rest-procs))
                      (cdr rest-procs))))]
    (let [procs (map analyze exps)]
      (if (null? procs)
        (Error. "Empty sequence -- ANALYZE"))
      (lop (car procs) (cdr procs)))))

(defn analyze-application [exp]
  (let [fproc (analyze (operator exp))
        aprocs (map analyze (operands exp))]
    (fn [env]
      (execute-application (fproc env)
                           (map (fn [aproc] (aproc env))
                                aprocs)))))

(defn analyze-self-evaluating [exp]
  (fn [env] exp))

(defn analyze-quoted [exp]
  (let [qval (text-of-quotation exp)]
    (fn [env] qval)))

(defn analyze-variable [exp]
  (fn [env] (lookup-variable-value exp env)))

(defn analyze-assignment [exp]
  (let [var (assignment-variable exp)
        vproc (analyze (assignment-value exp))]
    (fn [env]
      (set-variable-value! var (vproc env) env)
      'ok)))

(defn analyze-definition [exp]
  (let [var (definition-variable exp)
        vproc (analyze (definition-value exp))]
    (fn [env]
      (define-variable! var (vproc env) env)
      'ok)))

(defn analyze-if [exp]
  (let [pproc (analyze (if-predicate exp))
        cproc (analyze (if-consequent exp))
        aproc (analyze (if-alternative exp))]
    (fn [env]
      (if (true? (pproc env))
        (cproc env)
        (aproc env)))))

(defn analyze-lambda [exp]
  (let [vars (lambda-parameters exp)
        bproc (analyze-sequence (lambda-body exp))]
    (fn [env] (make-procedure vars bproc env))))

(def primitive-procedures
     (list (list 'car car)
           (list 'cdr cdr)
           (list 'cadr cadr)
           (list 'cons cons)
           (list 'null? null?)
           (list '+ +)
           (list '- -)
           (list '* *)
           (list '/ /)
           (list '= =)
           (list '> >)
           (list '< <)
           (list 'and (fn [& xs] (reduce #(and %1 %2) true xs)))
           (list 'or (fn [& xs] (reduce #(or %1 %2) false xs)))))

(defn primitive-procedure-names []
  (map car primitive-procedures))

(defn primitive-procedure-objects []
  (map (fn [proc] (list 'primitive (cadr proc)))
       primitive-procedures))

(defn setup-environment []
  (let [initial-env
        (extend-environment (primitive-procedure-names)
                            (primitive-procedure-objects)
                            the-empty-environment)]
    (define-variable! 'true true initial-env)
    (define-variable! 'false false initial-env)
    (define-variable! 'nil nil initial-env)
    initial-env))

(def the-global-environment (setup-environment))

(defn reset-global-environment []
  (def the-global-environment (setup-environment)))

(defn primitive-procedure? [proc]
  (tagged-list? proc 'primitive))

(defn primitive-implementation [proc] (cadr proc))

(defn apply-primitive-procedure [proc args]
  (apply (primitive-implementation proc) args))

(defn execute-application [proc args]
  (cond (primitive-procedure? proc)
          (apply-primitive-procedure proc args)
        (compound-procedure? proc)
          ((procedure-body proc)
           (extend-environment (procedure-parameters proc)
                               args
                               (procedure-environment proc)))
        :else
        (Error. (str
                 "Unknown procedure type -- EXECUTE-APPLICATION"
                 proc))))

(defn analyze [exp]
  (cond (self-evaluating? exp) 
        (analyze-self-evaluating exp)
        (quoted? exp) (analyze-quoted exp)
        (variable? exp) (analyze-variable exp)
        (assignment? exp) (analyze-assignment exp)
        (definition? exp) (analyze-definition exp)
        (if? exp) (analyze-if exp)
        (lambda? exp) (analyze-lambda exp)
        (begin? exp) (analyze-sequence (begin-actions exp))
        (cond? exp) (analyze (cond->if exp))
        (application? exp) (analyze-application exp)
        :else
        (Error. (str "Unknown expression type -- ANALYZE " exp))))

(defn my-eval [exp env]
  (cond (self-evaluating? exp) exp
        (variable? exp) (lookup-variable-value exp env)
        (quoted? exp) (text-of-quotation exp)
        (assignment? exp) (eval-assignment exp env)
        (definition? exp) (eval-definition exp env)
        (if? exp) (eval-if exp env)
        (lambda? exp)
          (make-procedure (lambda-parameters exp)
                          (lambda-body exp)
                          env)
        (begin? exp)
          (eval-sequence (begin-actions exp) env)
        (cond? exp) (my-eval (cond->if exp) env)
        (let? exp) (my-eval (let->combination exp) env)
        (let*? exp) (my-eval (let*->nested-lets exp) env)
        (application? exp)
          (my-apply (my-eval (operator exp) env)
                    (list-of-values (operands exp) env))
        :else (Error. (str "Unknown expression type -- EVAL " exp))))

(defn my-apply [procedure arguments]
  (cond (primitive-procedure? procedure)
          (apply-primitive-procedure procedure arguments)
        (compound-procedure? procedure)
          (eval-sequence
           (procedure-body procedure)
           (extend-environment
            (procedure-parameters procedure)
            arguments
            (procedure-environment procedure)))
        :else (Error. (str "Unknown procedure type -- APPLY " procedure))))

(defn interpret [exp]
  (my-eval exp the-global-environment))
