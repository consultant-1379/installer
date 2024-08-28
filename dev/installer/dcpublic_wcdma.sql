if (object_id('dc.generate_combined_view_dcpublic') is not null) then
    drop procedure dc.generate_combined_view_dcpublic ;
	
    if (object_id('dc.generate_combined_view_dcpublic') is not null) then
		message '<<< FAILED to drop procedure dc.generate_combined_view_dcpublic >>>' type info to log ;
		message '<<< FAILED to drop procedure dc.generate_combined_view_dcpublic >>>' type info to client ;
    else
        message '<<< Dropped procedure dc.generate_combined_view_dcpublic >>>' type info to log ;
		message '<<< Dropped procedure dc.generate_combined_view_dcpublic >>>' type info to client ;
    end if ;
end if ;

CREATE PROCEDURE dc.generate_combined_view_dcpublic(IN @viewname VARCHAR(256))
begin
        declare @tname1         varchar(256) ;
        declare @col1             varchar(256) ;
        declare @sql             LONG VARCHAR ;
		declare @tname11         varchar(256) ;
		declare @tname22         varchar(256) ;
		
CREATE TABLE #TempTableCol1(col varchar(2000));

INSERT INTO #TempTableCol1 (col) select distinct cname from sys.syscolumns where tname = @viewname and  creator = 'dc';


set @sql='create view dcpublic.' + @viewname + ' as '||CHAR(10);
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
         set @sql =  @sql||' from dc.'||@viewname;

set @sql =  @sql;


set @sql = REPLACE ( @sql , ',  from' , ' from' );

select @sql;
end;
