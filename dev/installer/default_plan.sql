if not exists (select 1 from PartitionPlan_temp where partitionplan='extralarge_raw') then
   insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('extralarge_raw',30,48,30,0) 
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='extralarge_plain') then  
   insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('extralarge_plain',30,48,30,0)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='extralarge_count') then
   insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('extralarge_count',30,48,30,0)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='extrasmall_raw') then
   insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('extrasmall_raw',90,744,90,0)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='extrasmall_count') then  
   insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('extrasmall_count',90,744,90,0) 
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='small_raw') then  
   insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('small_raw',90,384,90,0) 
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='small_count') then  
   insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('small_count',90,384,90,0) 
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='medium_raw') then  
   insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('medium_raw',90,168,90,0) 
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='medium_count') then  
   insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('medium_count',90,168,90,0) 
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='large_raw') then  
   insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('large_raw',90,168,90,0) 
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='large_count') then  
   insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('large_count',90,168,90,0) 
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='large_plain') then  
   insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('large_plain',120,168,120,0) 
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='extrasmall_plain') then  
   insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('extrasmall_plain',250,384,250,0) 
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='small_plain') then  
   insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('small_plain',250,384,250,0) 
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='medium_plain') then  
   insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('medium_plain',250,384,250,0) 
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='extralarge_day') then  
   insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('extralarge_day',400,744,400,0) 
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='extralarge_daybh') then  
   insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('extralarge_daybh',400,744,400,0) 
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='extralarge_rankbh') then  
   insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('extralarge_rankbh',400,744,400,0) 
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='extrasmall_day') then  
   insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('extrasmall_day',1095,9624,1095,0) 
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='extrasmall_daybh') then
   insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('extrasmall_daybh',1095,9624,1095,0) 
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='extrasmall_rankbh') then  
   insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('extrasmall_rankbh',1095,9624,1095,0) 
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='small_day') then
   insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('small_day',1095,4824,1095,0) 
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='small_daybh') then  
   insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('small_daybh',1095,4824,1095,0) 
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='small_rankbh') then  
   insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('small_rankbh',1095,4824,1095,0) 
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='medium_day') then  
   insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('medium_day',1095,2160,1095,0) 
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='medium_daybh') then  
   insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('medium_daybh',1095,2160,1095,0) 
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='medium_rankbh') then  
   insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('medium_rankbh',1095,2160,1095,0) 
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='large_day') then  
   insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('large_day',1095,2160,1095,0) 
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='large_daybh') then  
   insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('large_daybh',1095,2160,1095,0) 
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='large_rankbh') then  
   insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('large_rankbh',1095,2160,1095,0) 
end if;

--Partitions for ENIQ Events (VOLUME BASED)
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgeh_raw') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgeh_raw',3297309120,250000000,3297309120,1)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgeh_raw_lev2') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgeh_raw_lev2',29675782080,2500000000,29675782080,1)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehextralarge_raw') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehextralarge_raw',8243272800,200000000,8243272800,1)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehlarge_raw') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehlarge_raw',16486545600,800000000,16486545600,1)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehmedium_raw') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehmedium_raw',824327280,200000000,824327280,1)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehsmall_raw') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehsmall_raw',600000000,50000000,600000000,1)
end if;


--Partitions for ENIQ Events (TIME BASED)
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgeh_1min') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgeh_1min',14,168,14,0)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgeh_15min') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgeh_15min',14,168,14,0)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgeh_day') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgeh_day',1095,4320,1095,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehlarge_1min') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehlarge_1min',14,48,14,0)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehlarge_15min') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehlarge_15min',14,168,14,0)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehlarge_day') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehlarge_day',30,336,30,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehmedium_1min') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehmedium_1min',14,168,14,0)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehmedium_15min') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehmedium_15min',14,168,14,0)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehmedium_day') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehmedium_day',30,168,30,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehsmall_1min') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehsmall_1min',14,168,14,0)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehsmall_15min') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehsmall_15min',14,168,14,0)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehsmall_day') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehsmall_day',30,336,30,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehextralarge_1min') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehextralarge_1min',14,168,14,0)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehextralarge_15min') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehextralarge_15min',90,336,90,0)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehextralarge_day') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehextralarge_day',1095,2160,1095,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehextrasmall_1min') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehextrasmall_1min',14,168,14,0)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehextrasmall_15min') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehextrasmall_15min',30,168,30,0)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehextrasmall_day') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehextrasmall_day',1095,2160,1095,0)
end if;

--Partitions for NetAn (TIME BASED)
if not exists (select 1 from PartitionPlan_temp where partitionplan='cv_day') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('cv_day',1095,1800,1095,0)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='cv_raw') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('cv_raw',90,168,90,0)
end if;

--Partitions for ENIQ Stats (BULK CM)
if not exists (select 1 from PartitionPlan_temp where partitionplan='bulk_cm_raw') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('bulk_cm_raw',1095,4824,1095,0)
end if;