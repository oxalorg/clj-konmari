(ns konmari.inconsistent-aliases
  (:require [rewrite-clj.zip :as z]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(defn all-requires [f]
  (let [zroot (z/of-file f)
        require-form (-> zroot
                         (z/find-value z/next :require)
                         z/up)]
    (-> require-form
        z/sexpr
        rest)))

(defn require->map [req]
  (when (vector? req)
    (let [parts (partition 2 (rest req))]
      (merge
       {:ns (first req)}
       (into {}
             (mapv vec parts))))))

(defn all-requires->map [f]
  (for [req (all-requires f)]
    (require->map req)))

(defn requires-data [files]
  (->> files
       (mapcat all-requires->map)
       (group-by :ns)))

(defn find-mismatched-aliases [requires-data]
  (remove nil?
          (for [[ns req-list] requires-data]
            (let [unique-aliases (set (remove nil? (map :as req-list)))]
              (when (> (count unique-aliases) 1)
                [ns unique-aliases])))))

(defn find-files-recursively [dir]
  (->> (tree-seq #(.isDirectory %)
                 #(.listFiles %)
                 (io/file dir))
       (remove #(.isDirectory %))
       (filter #(re-find #"\.clj$" (str %)))))

(println "Running analysis on " (first *command-line-args*))

(def final
  (-> (find-files-recursively (first *command-line-args*))
      requires-data
      find-mismatched-aliases))

(doseq [[ns duplicates] final]
  (println
   (format "%-50s -> %s" ns (str/join ", " duplicates))))
