(ns quote-loader.core
  (:require [clojure.java.io :as io]
            [clojure.java.jdbc :as sql]
            [clojure.string :as str]
            [clojure-csv.core :as csv]
            [clojure.tools.cli :refer [cli]])
  (:gen-class :main true))

;; ----------------------------------------------------------------------------------------------------
;; Data Downloading
;; ----------------------------------------------------------------------------------------------------

(defn clean-symbol
  [sym]
  (str/trim (str/lower-case sym)))

(defn build-url [sym]
  (str "http://ichart.finance.yahoo.com/table.csv?s=" sym "&ignore=.csv"))

(defn download-historical-quotes 
  "Build the url to get the quotes for 'sym' and return the results"
  [sym]
  (let [sym (clean-symbol sym)
        url (build-url sym)
        filename (str sym ".csv")
        result []]
    (try 
      (with-open [rdr (io/reader url)]
        ;; realize the sequence returned from reader so we can use the data outside of this routine
        ;; note, the first line will be a header
        ;; "Date,Open,High,Low,Close,Volume,Adj Close"
        ;;"2015-11-10,724.400024,730.590027,718.50,728.320007,1603900,728.320007"
        (doall (line-seq rdr)))
      (catch Exception e (println (format "Error downloading '%s'" sym))))))

;; ----------------------------------------------------------------------------------------------------
;; Database
;; ----------------------------------------------------------------------------------------------------

(def db-host "localhost")
(def db-port 3306) ;; WAMP == 3306 MAMP == 8889
(def db-name "quote")
(def db-user "tester")
(def db-pwd "password")

(def database-dev-settings
  {:classname "com.mysql.jdbc.Driver" ; must be in classpath
   :subprotocol "mysql"
   :subname (str "//" db-host ":" db-port "/" db-name)
   :user db-user
   :password db-pwd})

(defn parse-row [row]
  (let [v (first (csv/parse-csv row))]
    (zipmap [:date :open :high :low :close :vol :adjclose] v)))

(defn valid-data [str]
  (Character/isDigit (first str)))

(defn insert-quote
  "Insert or update a quote passed in as a map"
  [sym q]
  (sql/insert! database-dev-settings :quote
               {:symbol sym :date (:date q) :open (:open q) :high (:high q) :low (:low q) :close (:close q) :vol (:vol q) :adjclose (:adjclose q)}))

(defn insert-quote0
  "Insert or update a quote passed in as a map"
  [sym q]
  (sql/update! database-dev-settings
                               :quote
                               ["symbol=? and date=?" sym (:date q)]
                               {:symbol sym :date (:date q) :open (:open q) :high (:high q) :low (:low q) :close (:close q) :vol (:vol q) :adjclose (:adjclose q)}))

(defn insert-quote1
  "Insert or update a quote passed in as a map.
We can improve the readability of the code by using assoc to build a new map to mass into update-or-insert-values. Realizing that the new map will have all the same keys as our parameter sym with the addition of a new key value of :sumbol lets us use assoc to build the new map with (assoc q :symbol sym)"
  [sym q]
  (sql/update! database-dev-settings
                               :quote
                               ["symbol=? and date=?" sym (:date q)]
                               (assoc q :symbol sym)))

(defn insert-quote2
  "Insert or update a quote passed in as a map.
Destructoring alls you to create variables of the map's value as we pass the map into our function. With this we create variables sym, date, open, high, low, close, vol and adjclose upon entering our function. Then when we create a map to pass to the update-or-insert-values we are creating one with values rather than pulling apart our map in multiple calls.
"
  [sym {date :date
        open :open
        high :high
        low :low
        close :close
        vol :vol
        adjclose :adjclose}]
  (sql/update! database-dev-settings
                               :quote
                               ["symbol=? and date=?" sym date]
                               {:symbol sym :date date :open open :high high :low low :close close :vol vol :adjclose adjclose}))

(defn load-historical-quotes [sym]
  (let [sym (clean-symbol sym)
        rows (download-historical-quotes sym)]
    ;; use doseq to effect the side-effect of insert-quote
    ;; ignore the first line, header by calling (rest rows)
    (doseq [row (rest rows)]
      (insert-quote sym (parse-row row)))))

;; ----------------------------------------------------------------------------------------------------
;; Other database
;; ----------------------------------------------------------------------------------------------------

(defn select-quotes
  "Simple select * from quote table to see the table's contents"
  ([]
   (let [results (sql/query database-dev-settings "select * from quote")]
     (dorun (map println results))))
  ([sym]
   (let [results (sql/query database-dev-settings (format "select * from quote where symbol = '%s' " sym))]
     (dorun (map println results)))))

(defn delete-quotes
  "Truncate the quote table or remove quotes by symbol"
  ([] (sql/delete! database-dev-settings :quote []))
  ([sym] (sql/delete! database-dev-settings :quote ["symbol = ?" sym])))

(defn list-symbols
  "Return the distinct list of symbols in the quote table"
  []
  (let [results (sql/query database-dev-settings "select distinct(symbol) from quote")
        symbols (map (fn [q] (:symbol q)) results)]
    (dorun (map println symbols))))

(defn print-usage []
  (println "quote-loader SYMBOL"))

(defn process-symbols-sequential [lst]
  (doseq [sym lst]
    (load-historical-quotes sym)))

(defn process-symbols-parallel [lst]
  (do 
    (dorun (pmap (fn [sym] (load-historical-quotes sym)) lst))
    (shutdown-agents)))

(defn print-usage [options-summary]
  (println (->> [""
                 "Usage: quote-loader [options] [symbols]"
                 ""
                 "Options:"
                 options-summary
                 ""
                 "Symbols:"
                 "Optional list of symbols to load"
                 "Use -f option as an alternative to load a larger number of symbols"
                 ""]
                (clojure.string/join \newline))))

(defn load-symbol-file 
  "Read a file of symbols and return them in a sequence"
  [filename]
  (with-open [rdr (io/reader filename)]
    (filter #(not (empty? %)) (doall (line-seq rdr)))))

(defn run-command 
  [cmd sym]
  (println "run-command" cmd)
  (if (= cmd "select") (println "select"))
  (cond
    (= cmd "select") (if sym (select-quotes sym) (select-quotes))
    (= cmd "delete") (if sym (delete-quotes sym) (delete-quotes))
    (= cmd "symbols") (list-symbols)
    :else (println "Unknown command. Valid commands are select, delete, symbols")))

(defn -main [& args]
  (let [[opts args summary]
        ;; https://github.com/bradlucas/cmdline/blob/master/src/cmdline/core.clj
        (cli args
             ["-p" "--parallel" "Parrallel loading" :flag true :default false]
             ["-f" "--file-symbols" "File containing symbols to load (overloads symbols passed as args)"]
             ["-c" "--command" "Commands: select [sym], delete [sym], symbols"])]
    ;; (println "opts: " opts)
    ;; (println "args: " args)
    (if (:command opts)
      (run-command (:command opts) (first args))
      (if (and (not (:file-symbols opts)) (not (seq args)))
        (print-usage summary)
        (do
          (let [parallel (:parallel opts)
                symbols (if (:file-symbols opts) (load-symbol-file (:file-symbols opts)) args)]
            (let [time-msg (with-out-str (time (if parallel (process-symbols-parallel symbols) (process-symbols-sequential symbols))))]
              (println time-msg))))))))

;; ----------------------------------------------------------------------------------------------------
;; Plan
;;
;; TODO
;; connection pooling
;;    http://clojure-doc.org/articles/ecosystem/java_jdbc/connection_pooling.html
;; document different insert/update functions and test each
;;
;; http://clojure-doc.org/articles/ecosystem/java_jdbc/home.html
;; http://clojure.github.io/java.jdbc/#clojure.java.jdbc/query
