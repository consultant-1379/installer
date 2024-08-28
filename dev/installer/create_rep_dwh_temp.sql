IF NOT EXISTS (SELECT 1 FROM SYSTABLE WHERE TABLE_NAME = 'dwh_repdb_count') THEN
create table dwh_repdb_count (
 execution_date date NOT NULL,
 execution_time datetime NOT NULL,
 rep_dwh_name varchar(5) NOT NULL,
 no_of_connection unsigned integer NOT NULL
)
ELSEIF (select count(*) from syscolumn sc ,systable st where sc.table_id = st.table_id and st.table_name = 'dwh_repdb_count') =3  THEN
alter table dwh_repdb_count 
add execution_date date NULL,
add execution_time datetime NULL,
alter execution_date_time NULL
END IF;
go
