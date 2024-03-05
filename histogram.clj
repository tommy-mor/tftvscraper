(require '[clojure.edn :as edn]
         '[clojure.pprint :as pp])

(def data (edn/read-string (slurp "all.edn")))

(doall (for [x data]
         (println (:frags x))))




