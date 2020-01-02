(ns darkleaf.effect.util
  (:require
   [darkleaf.effect.core :refer [break !]])
  #?(:cljs (:require-macros [darkleaf.effect.util :refer [->!]])))

(defmacro ->! [x & forms]
  (let [forms! (map (fn [form] `(-> ~form !))
                    forms)]
    `(-> (! ~x) ~@forms!)))

(defn reduce!
  ([ef coll]
   (break
     (case (count coll)
       0 (! (ef))
       1 (first coll)
       (! (reduce! ef (first coll) (rest coll))))))
  ([ef val coll]
   (break
     (loop [acc val
            coll coll]
       (cond
         (reduced? acc)
         (unreduced acc)

         (empty? coll)
         acc

         :else
         (recur (! (ef acc (first coll)))
                (rest coll)))))))

(defn mapv!
  ([ef coll]
   (break
     (let [reducer (fn [acc item]
                     (break
                       (conj! acc (! (ef item)))))
           acc     (transient [])
           result  (! (reduce! reducer acc coll))]
       (persistent! result))))
  ([ef coll & colls]
   (->> (apply map list coll colls)
        (mapv! #(apply ef %)))))
