(require '[babashka.pods :as pods]
         '[babashka.curl :as curl]
         '[clojure.string :as str]
         '[com.rpl.specter :refer :all])

(pods/load-pod 'retrogradeorbit/bootleg "0.1.9")

(require '[pod.retrogradeorbit.bootleg.utils :refer [convert-to]]
         '[pod.retrogradeorbit.hickory.select :as s])

(defn yt? [x] (or (str/starts-with? x "https://www.youtube.com/watch")
                  (str/starts-with? x "https://www.youtu.be/")
                  (str/starts-with? x "http://www.youtu.be/")
                  (str/starts-with? x "http://www.youtube.com/watch")
                  (str/starts-with? x "https://youtube.com/watch")
                  (str/starts-with? x "http://youtube.com/watch")
                  (str/starts-with? x "https://youtu.be/")))

(defn parse-page [pagenumber]
  

  (def url (str "https://www.teamfortress.tv/441/frag-clips-thread?page=" pagenumber))

  (def index
    (-> (curl/get url)
        :body
        str/trim
        (convert-to :hickory)))

  (def numbers
    (->> index
         (s/select (s/class "post-num"))
         (map :content)
         (map first)))

  (def pages
    (->> index
         (s/select (s/class "post-author"))
         (map :content)
         (map first)))
  
  (def frags
    (->> index
         (s/select (s/class "post-frag-count"))
         (map :content)
         (map first)
         (map str/trim)
         (map #(Integer/parseInt %))))
  (def bodies
    (->> index
         (s/select (s/class "post-body"))
         (map :content)
         (map (fn [x] (map #(s/select (s/tag :a) %) x)))
         (map flatten)
         (transform [ALL ALL] (comp :href :attrs))
         (transform [ALL] #(filter yt? %))
         (map #(hash-map :number %1 :author %2 :frags %3 :links %4) numbers pages frags)))

  (doseq [x bodies]
    (pr x)
    (println)))

(println "[")
(doall (pmap (fn [page]
               (binding [*out* *err*]
                 (println (str "doing page " page)))
               (parse-page page))
             
             (range 321)))
(println "]")


