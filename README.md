quote-loader
========================================

A simple yahoo quote loader described in the article at http://beaconhill.com/solutions/kb/clojure/loading-data-with-clojure.html.

Setup
========================================

The example requires that you have a database. For this example MySQL is assumed.

See the init.sql and table.sql files in the sql directory. The init.sql will create a quote database, a test user and the quote table required for the example.

Example Usage
========================================

With your quote database setup the following should download and load stock quotes from Yahoo.

1. Build standalone version with 'lein ubuerjar'
2. From the command line enter 'java -jar quote-loader-1.0.0-standalone.jar'
   followed by one or more stock symbols.
3. For example, java -jar quote-loader-1.0.0-standalone.jar goog aapl
4. Look in your quote table.
   select * from quote where symbol='goog';
   select * from quote where symbol='aapl

Comment
========================================

Currently there is no error checking for valid symbols so for now only put in valid symbols.


Change Log
========================================

* Version 1.0.0


Copyright and License
========================================

Copyright (c) Brad Lucas, 2012. All rights reserved.  The use and
distribution terms for this software are covered by the Eclipse Public
License 1.0 (http://opensource.org/licenses/eclipse-1.0.php) which can
be found in the file epl.html at the root of this distribution.
By using this software in any fashion, you are agreeing to be bound by
the terms of this license.  You must not remove this notice, or any
other, from this software.
