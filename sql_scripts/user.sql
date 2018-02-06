
CREATE TABLESPACE tp_tables
DATAFILE  '\u01\app\oracle\oradata\orcl12\orcl\tp_tables_01.dbf'
SIZE 100M;


CREATE TEMPORARY TABLESPACE tp_temp
TEMPFILE  '\u01\app\oracle\oradata\orcl12\orcl\tp_temp_01.dbf'
SIZE 50M 
AUTOEXTEND ON;

select * from dba_tablespaces;

CREATE USER Manager
    IDENTIFIED BY pass
    DEFAULT TABLESPACE tp_tables
    TEMPORARY TABLESPACE tp_temp;
    GRANT CONNECT TO Manager
    
    

GRANT CREATE SESSION TO Manager;

GRANT CREATE table TO Manager;

GRANT CREATE view TO Manager;

GRANT DROP ANY table TO Manager;

GRANT CREATE procedure TO Manager;

GRANT ALTER ANY table TO  Manager;

GRANT CREATE ANY TRIGGER TO Manager;

ALTER USER Manager QUOTA 300M ON tp_tables;

GRANT UPDATE ANY TABLE TO Manager ; 
