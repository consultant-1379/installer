if (object_id('dwhrep.generate_DIM_E_LTE_LLE_ERBS_view') is not null) then
    drop procedure dwhrep.generate_DIM_E_LTE_LLE_ERBS_view ;
    if (object_id('dwhrep.generate_DIM_E_LTE_LLE_ERBS_view') is not null) then
		message '<<< FAILED to drop procedure dwhrep.generate_DIM_E_LTE_LLE_ERBS_view >>>' type info to log ;
		message '<<< FAILED to drop procedure dwhrep.generate_DIM_E_LTE_LLE_ERBS_view >>>' type info to client ;
    else
        message '<<< Dropped procedure dwhrep.generate_DIM_E_LTE_LLE_ERBS_view >>>' type info to log ;
		message '<<< Dropped procedure dwhrep.generate_DIM_E_LTE_LLE_ERBS_view >>>' type info to client ;
    end if ;
end if ;

CREATE PROCEDURE dwhrep.generate_DIM_E_LTE_LLE_ERBS_view()
begin
        declare @tname1         varchar(128);
        declare @col1             varchar(128);
        declare @sql             LONG VARCHAR;    

set @sql='create view dc.DIM_E_LTE_LLE_ERBS as '||CHAR(10);

FOR loop1 AS Curs1 CURSOR FOR
        select TABLENAME from DWHPartition where STORAGEID like 'DC_E_ERBS_EUTRANCELLFDD:RAW' 
do
        set @tname1 = TABLENAME ; 
        set @sql =  @sql||'select distinct ERBS from dc.'||@tname1||' union '||CHAR(10);

end for ;
set @sql = LEFT( @sql , LEN(@sql) - 7)  ; 
select @sql;
end;