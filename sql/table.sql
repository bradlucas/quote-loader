drop table if exists quote;
create table quote (
       quote_id    int(10) unsigned not null auto_increment,
       symbol      varchar(6),
       date        date,
       open        decimal(18,4),
       high        decimal(18,4),
       low         decimal(18,4),
       close       decimal(18,4),
       vol         bigint,
       adjclose    decimal(18,4),
       primary key (quote_id),
       index (symbol, date)
);

-- YYYY-MM-DD  http://dev.mysql.com/doc/refman/5.1/en/datetime.html
-- http://stackoverflow.com/questions/1523576/database-schema-for-organizing-historical-stock-data
