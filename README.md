# quote-loader

[![Clojars Project](https://img.shields.io/clojars/v/com.bradlucas/quote-loader.svg)](https://clojars.org/com.bradlucas/quote-loader)

A simple yahoo quote loader initially described in the article at http://beaconhilltechnologysolutions.com/solutions/kb/clojure/loading-data-with-clojure.html.

The project has been updated since the article. Since the article the following enhancements have been made.

- Download function will catch an error if passed an invalid symbol
- Parallel version of process function as well as original sequential version
- Command line options
 - -p for parallel loading (default is sequential)
 - -f FILENAME for loading from a file containing symbols
 - -c COMMAND where command is select, delete or symbols
   - select and delete take an optional [SYMBOL] parameter

# Lein Examples
========================================

Load all quotes for Google

lein run goog

Load all quotes for symbols in file 'symbols.txt'

lein run -f symbols.txt

Show quotes for Google

lein run -c select goog

Show all quotes in the databsae

lein run -c select

Show a list of the symbols in the database (select distinct(symbol)...)

lein run -c symbols

Delete all goog quotes

lein run -c delete goog

Delete all quotes in the database (truncate quotes)

lein run -c delete



# Database Setup
========================================

The example requires that you have a database. For this example MySQL is assumed.

See the init.sql and table.sql files in the sql directory. The init.sql will create a quote database, a test user and the quote table required for the example.

# Example Usage
========================================

With your quote database setup the following should download and load stock quotes from Yahoo.

1. Build standalone version with 'lein ubuerjar'
2. From the command line enter 'java -jar target/quote-loader-1.0.0-standalone.jar'
   followed by one or more stock symbols.
3. For example, java -jar target/quote-loader-1.0.0-standalone.jar goog aapl
4. Look in your quote table.
   select * from quote where symbol='goog';
   select * from quote where symbol='aapl

# Comment
========================================

Currently there is no error checking for valid symbols so for now only put in valid symbols.


# Change Log
========================================

* Version 1

* Version 2

The java.jdbc library changed it's API and removed with-connection

The new since 0.3.0 has the db connection information passed as a parameter

@see http://stackoverflow.com/questions/22586804/with-connection-what-happened

project.clj [org.clojure/java.jdbc "0.4.2"]

core.clj change the sql/insert! and sql/update! calls to remove with-connection

* Version 3

  - time functions
  - separate download of data from db action
  - query functions
  - database functions
    -   select *, select symbol
    -   truncate quote, delete from quote where symbol
    -   list distinct symbols
  - parallel version, default to sequential
  - file loading option
  - command line options
    -  seq/par
    -  db functions
  - default to lower case symbol in all cases
  - command line command for sql functions
    -   -c select-quotes [SYM]
    -   -c delete-quotes [SYM]
    -   -c list-smbols




# Copyright and License
========================================

Copyright (c) Brad Lucas, 2012. All rights reserved.  The use and
distribution terms for this software are covered by the Eclipse Public
License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which can
be found in the file epl.html at the root of this distribution.
By using this software in any fashion, you are agreeing to be bound by
the terms of this license.  You must not remove this notice, or any
other, from this software.
