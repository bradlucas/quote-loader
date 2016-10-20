(defproject org.clojars.blucas/quote-loader "1.0.1"
  :description "quote-loader"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clojure-csv/clojure-csv "2.0.1"]
                 [org.clojure/java.jdbc "0.6.2-alpha3"]
                 [mysql/mysql-connector-java "6.0.5"]
                 [org.clojure/tools.cli "0.3.5"]]
  :main quote-loader.core
  ;; :jvm-opts ["-Xmx768M"]
  :jvm-opts ["-Xmx1g"]
  )
