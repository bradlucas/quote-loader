(ns quote-loader.core
  (:require [clojure.java.io :as io])
  (:require [clojure.java.jdbc :as sql])
  (:require [clojure.string :as str])
  (:require [clojure-csv.core :as csv])
  (:gen-class :main true))

;; ----------------------------------------------------------------------------------------------------
;; Data Downloading
;; ----------------------------------------------------------------------------------------------------

(defn build-url [sym]
  (str "http://ichart.finance.yahoo.com/table.csv?s=" sym "&ignore=.csv"))

(defn download-historical-quotes 
  "Build the url to get the quotes for 'sym' and return the results"
  [sym]
  (let [url (build-url sym)
        filename (str sym ".csv")
        result []]
    (println filename)
    (println url)
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
  (let [rows (download-historical-quotes sym)]
    ;; use doseq to effect the side-effect of insert-quote
    ;; ignore the first line, header by calling (rest rows)
    (doseq [row (rest rows)]
      (insert-quote sym (parse-row row)))))

;; ----------------------------------------------------------------------------------------------------
;; Other database
;; ----------------------------------------------------------------------------------------------------

(defn query-quote-table 
  "Simple select * from quote table to see the table's contents"
  ([]
   (let [results (sql/query database-dev-settings "select * from quote")]
     (dorun (map println results))))
  ([sym]
   (let [results (sql/query database-dev-settings (format "select * from quote where symbol = '%s' " sym))]
     (dorun (map println results)))))


(defn print-usage []
  (println "quote-loader SYMBOL"))

(defn process-symbols [lst]
  (doseq [sym lst]
    (println "entering process-symbols")
    (load-historical-quotes sym)
    (println "exiting process-symbols")
    ))

(defn -main [& args]
  ;; accept a list of stock symbols as arguments otherwise print a usage statement
  (if args 
    (let [time-msg (time (process-symbols args))]
      (println time-msg))
    (print-usage)))


;; ----------------------------------------------------------------------------------------------------
;; Plan
;; time functions
;; separate download of data from db action
;; query functions
;;  default to lower case symbol in all cases
;;
;; document different insert/update functions and test each
;; parallel version
;; file loading option
;; clear database (whole table, just specific ticker)


;; http://clojure-doc.org/articles/ecosystem/java_jdbc/home.html
;; http://clojure.github.io/java.jdbc/#clojure.java.jdbc/query
