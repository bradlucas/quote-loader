create database quote;

create user 'tester'@'localhost' identified by 'password';

grant all on quote.* to 'tester'@'localhost';
