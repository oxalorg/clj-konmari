(ns konmari.inconsistent-aliases
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.tools.cli :as cli]
            [rewrite-clj.zip :as z]))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn all-requires
  "Parses and returns all require forms in a file
  eg: '[[clojure.string :as str]
        [...]
        ...]"
  [f]
  (let [zroot (z/of-file f)
        require-form (-> zroot
                         (z/find-value z/next :require)
                         z/up)]
    (-> require-form
        z/sexpr
        rest)))

(defn require->map
  "Converts a require form into a map.
  Eg: `'[clojure.string :as str :refer [join] :as-alias str]`
  will return `{:ns clojure.string
                :as str
                :refer [join]
                :as-alias str}`"
  [req]
  (when (vector? req)
    (let [parts (partition 2 (rest req))]
      (merge
       {:ns (first req)}
       (into {}
             (mapv vec parts))))))

(defn all-requires->map [f]
  (map require->map (all-requires f)))

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

(defn find-files-recursively
  "Finds all `.clj` files recursively in dir"
  [dir]
  (->> (tree-seq #(.isDirectory %)
                 #(.listFiles %)
                 (io/file dir))
       (remove #(.isDirectory %))
       (filter #(re-find #"\.clj$" (str %)))))

(defn find-inconsistent-aliases [dir]
  (-> (find-files-recursively dir)
      requires-data
      find-mismatched-aliases))

(defn print-inconsistent-aliases [dir]
  (println "Running analysis on " dir)
  (let [final (find-inconsistent-aliases dir)]
    (doseq [[ns duplicates] final]
      (println
       (format "%-50s -> %s" ns (str/join ", " duplicates))))))

(defn read-int-from-user [choices-count]
  (loop [input (read-line)]
    (let [val (try
                (let [val (Integer/parseInt input)]
                  (when-not (<= 1 val choices-count)
                    (throw (Exception.)))
                  val)
                (catch Exception _
                  (println "Please select one of the above options. Try again: ")
                  (flush)
                  nil))]
      (if val
        val
        (recur (read-line))))))

(defn choose-preferred-aliases [dir]
  (println "Running analysis on " dir)
  (let [final (find-inconsistent-aliases dir)
        preferred-aliases (atom {})
        total (count final)]
    (doseq [[idx [ns duplicates]] (map-indexed vector final)]
      (println (format "%3d/%-3d %-70s" (inc idx) total ns))
      (let [aliases (vec duplicates)
            aliases-choices (map-indexed
                             #(str "    " (inc %1) ": " %2)
                             duplicates)
            choices-count (count aliases-choices)]
        (println (str/join "\n" aliases-choices))
        (println (str "    " (+ 1 choices-count) "[ignore]"))
        (println (str "    " (+ 2 choices-count) "[custom]"))
        (print "Choose: ")
        (flush)
        (let [user-choice (read-int-from-user (+ 2 choices-count))]
          (let [chosen (cond
                         (<= 1 user-choice choices-count)
                         (swap! preferred-aliases assoc ns (nth aliases (dec user-choice)))

                         (= user-choice (+ 1 choices-count))
                         :ignore

                         (= user-choice (+ 2 choices-count))
                         (do
                           (print "Enter a custom alias: ")
                           (flush)
                           (swap! preferred-aliases assoc ns (read-line))))])
          (println "~~~"))))
    (spit "preferred_aliases.edn" @preferred-aliases)))

(def cli-opts
  [["-c" "--choose" "Choose preferred alias and output a kondo config"]
   ["-h" "--help"]])

(defn get-opts [args]
  (cli/parse-opts args cli-opts))

(defn usage [opts-summary]
  opts-summary)

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (str/join \newline errors)))

(defn validate-args
  [args]
  (let [{:keys [options arguments errors summary]} (get-opts args)]
    (cond
      (:help options) ; help => exit OK with usage summary
      {:exit-message (usage summary) :ok? true}
      errors ; errors => exit with description of errors
      {:exit-message (error-msg errors)}
      ;; custom validation on arguments
      (= 1 (count arguments))
      {:dir (first arguments) :options options}
      :else ; failed custom validation => exit with usage summary
      {:exit-message (usage summary)})))

(defn -main [& args]
  (let [{:keys [dir options exit-message ok?]} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (if (:choose options)
        (choose-preferred-aliases dir)
        (print-inconsistent-aliases dir)))))

(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))
