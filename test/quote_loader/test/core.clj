(ns quote-loader.test.core
  (:use [quote-loader.core])
  (:use [clojure.test]))

(deftest replace-me ;; FIXME: write
  (is false "No tests have been written."))


;; The following are test function
;; idea: macro to create loader, accepts either the url bit or the file
(defn load-from-file [sym]
  (with-open [rdr (io/reader (str sym ".csv"))]
    (doseq [line (line-seq rdr) ]
      (if (valid-data line)
        (insert-quote sym (parse-row line)))))))


(defn get-row [file]
  (with-open [rdr (io/reader file)]
    (let [seq (line-seq rdr)]
      (second seq))))
