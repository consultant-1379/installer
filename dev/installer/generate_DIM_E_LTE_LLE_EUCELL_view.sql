if (object_id('dwhrep.generate_DIM_E_LTE_LLE_EUCELL_view') is not null) then
    drop procedure dwhrep.generate_DIM_E_LTE_LLE_EUCELL_view ;
	
    if (object_id('dwhrep.generate_DIM_E_LTE_LLE_EUCELL_view') is not null) then
		message '<<< FAILED to drop procedure dwhrep.generate_DIM_E_LTE_LLE_EUCELL_view >>>' type info to log ;
		message '<<< FAILED to drop procedure dwhrep.generate_DIM_E_LTE_LLE_EUCELL_view >>>' type info to client ;
    else
        message '<<< Dropped procedure dwhrep.generate_DIM_E_LTE_LLE_EUCELL_view >>>' type info to log ;
		message '<<< Dropped procedure dwhrep.generate_DIM_E_LTE_LLE_EUCELL_view >>>' type info to client ;
    end if ;
end if ;

CREATE PROCEDURE dwhrep.generate_DIM_E_LTE_LLE_EUCELL_view()
begin
        declare @tname1         varchar(128);
        declare @col1             varchar(128);
        declare @sql             LONG VARCHAR;    

set @sql='create view dc.DIM_E_LTE_LLE_EUCELL as '||CHAR(10);

FOR loop1 AS Curs1 CURSOR FOR
        select TABLENAME from DWHPartition where STORAGEID like 'DC_E_ERBS_EUTRANCELLFDD:RAW' 
do
        set @tname1 = TABLENAME ; 
	set @sql = @sql||'select distinct DC.DIM_E_LTE_EUCELL_CELL.OSS_ID, ERBS_ID, EUtranCellId, hostingDigitalUnit from DC.DIM_E_LTE_EUCELL_CELL, DC.'||@tname1||' where DC.DIM_E_LTE_EUCELL_CELL.OSS_ID=DC.'||@tname1||'.OSS_ID and DC.DIM_E_LTE_EUCELL_CELL.ERBS_ID=DC.'||@tname1||'.ERBS and DC.DIM_E_LTE_EUCELL_CELL.EUtranCellId=DC.'||@tname1||'.EUtranCellFDD union '||CHAR(10);

end for ;
set @sql = LEFT( @sql , LEN(@sql) - 7)  ; 
select @sql;
end;