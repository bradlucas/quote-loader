(ns quote-loader.core
  (:require [clojure.java.io :as io])
  (:require [clojure.java.jdbc :as sql])
  (:require [clojure.string :as str])
  (:require [clojure-csv.core :as csv])
  (:gen-class :main true))

(defn build-url [sym]
  (str "http://ichart.finance.yahoo.com/table.csv?s=" sym "&ignore=.csv"))

(def db-host "localhost")
(def db-port 8889)
(def db-name "quote")
(def db-user "tester")
(def db-pwd "password")

(def database-dev-settings
  {
   :classname "com.mysql.jdbc.Driver" ; must be in classpath
   :subprotocol "mysql"
   :subname (str "//" db-host ":" db-port "/" db-name)
   :user db-user
   :password db-pwd
  }
)

(defn parse-row [row]
  (let [v (first (csv/parse-csv row))]
    (zipmap [:date :open :high :low :close :vol :adjclose] v)))

(defn valid-data [str]
  (Character/isDigit (first str)))

(defn insert-quote
  "Insert or update a quote passed in as a map"
  [sym q]
    (sql/with-connection database-dev-settings
      (sql/update-or-insert-values
       :quote
       ["symbol=? and date=?" sym (:date q)]
       {:symbol sym :date (:date q) :open (:open q) :high (:high q) :low (:low q) :close (:close q) :vol (:vol q) :adjclose (:adjclose q)})))


(defn load-historical-quotes [sym]
  (let [url (build-url sym)]
    (with-open [rdr (io/reader url)]
      (doseq [line (line-seq rdr)]
        (if (valid-data line)
          (insert-quote sym (parse-row line)))))))


(defn print-usage []
  (println "quote-loader SYMBOL"))

(defn process-symbols [lst]
  (doseq [sym lst]
   (load-historical-quotes sym)))

(defn -main [& args]
  ;; accept a list of stock symbols as arguments otherwise print a usage statement
  (if args (process-symbols args)
      (print-usage)))
