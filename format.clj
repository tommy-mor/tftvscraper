(require '[clojure.edn :as edn]
         '[clojure.pprint :as pp])

(def input (slurp "all.edn"))
(def data (edn/read-string input))

(pp/pprint (sort-by :frags data))
