if (object_id('dwhrep.generate_erbs_combined_view') is not null) then
    drop procedure dwhrep.generate_erbs_combined_view ;
	
    if (object_id('dwhrep.generate_erbs_combined_view') is not null) then
		message '<<< FAILED to drop procedure dwhrep.generate_erbs_combined_view >>>' type info to log ;
		message '<<< FAILED to drop procedure dwhrep.generate_erbs_combined_view >>>' type info to client ;
    else
        message '<<< Dropped procedure dwhrep.generate_erbs_combined_view >>>' type info to log ;
		message '<<< Dropped procedure dwhrep.generate_erbs_combined_view >>>' type info to client ;
    end if ;
end if ;


CREATE PROCEDURE dwhrep.generate_erbs_combined_view( IN @table1 VARCHAR(256), IN @table2 VARCHAR(256), IN @viewname VARCHAR(256))
begin
        declare @tname1         varchar(256) ;
        declare @col1             varchar(256) ;
        declare @sql             LONG VARCHAR ;
		declare @tname11         varchar(256) ;
		declare @tname22         varchar(256) ;
		
CREATE TABLE #TempTableCol1(col varchar(2000));
CREATE TABLE #TempTableCol2(col varchar(2000));

set @tname11  = @table1;
set @tname22  = @table2;

INSERT INTO #TempTableCol1 (col) SELECT DATANAME as c1 FROM DWHColumn where STORAGEID = @tname11;
INSERT INTO #TempTableCol1 (col) SELECT DATANAME||'$' as c2 FROM DWHColumn where STORAGEID = @tname22 and DATANAME not in ( SELECT DATANAME FROM DWHColumn where STORAGEID = @tname11 ) ;
INSERT INTO #TempTableCol2 (col) SELECT DATANAME as c3 FROM DWHColumn where STORAGEID = @tname22;
INSERT INTO #TempTableCol2 (col) SELECT DATANAME||'$' as c4 FROM DWHColumn where STORAGEID = @tname11 and DATANAME not in ( SELECT DATANAME FROM DWHColumn where STORAGEID = @tname22 ) ;

set @sql='create view ' + @viewname + ' as '||CHAR(10);

FOR loop1 AS Curs1 CURSOR FOR
        select TABLENAME from DWHPartition where STORAGEID = @tname11 
do
        set @tname1 = TABLENAME ;
        
        set @sql=@sql||' select '; 
        FOR loop2 AS Curs2 CURSOR FOR
                select col from #TempTableCol1 order by col ASC 
                do
                  set @col1=( select case
                                        when col like '%$' then ' NULL as '||replace(col,'$','')
                                        else col
                                     end);
                  set @sql=@sql||@col1||', ';     
         end for ;
         set @sql =  @sql||' from dc.'||@tname1||' union all '||CHAR(10);
end for ;
set @sql =  @sql||CHAR(10);

FOR loop3 AS Curs3 CURSOR FOR
        select TABLENAME from DWHPartition where STORAGEID = @tname22 
do
        set @tname1 = TABLENAME ;
        
        set @sql=@sql||' select '; 
        FOR loop4 AS Curs4 CURSOR FOR
                select col from #TempTableCol2 order by col ASC 
                do
                  set @col1=( select case
                                        when col like '%$' then ' NULL as '||replace(col,'$','')
                                        else col
                                     end);
                  set @sql=@sql||@col1||', ';        
         end for ;
         set @sql =  @sql||' from dc.'||@tname1||' union all '||CHAR(10);
end for ;

set @sql = REPLACE ( @sql , ',  from' , ' from' );
set @sql = LEFT( @sql , LEN(@sql) - 11)  ; 

select @sql;
end;