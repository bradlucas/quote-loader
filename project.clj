(defproject org.clojars.blucas/quote-loader "1.0.0"
  :description "quote-loader"
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [clojure-csv/clojure-csv "2.0.1"]
                 [org.clojure/java.jdbc "0.2.3"]
                 ;; TODO java.jdbc changed API after 0.2.3
                 ;; latest is 
                 ;; [org.clojure/java.jdbc "0.4.1"]
                 [mysql/mysql-connector-java "5.1.36"]]
  :main quote-loader.core)
