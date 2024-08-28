if not exists (select 1 from PartitionPlan_temp where partitionplan='extrasmall_raw') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('extrasmall_raw',30,744,90,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='extrasmall_day') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('extrasmall_day',400,9624,1095,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='extrasmall_daybh') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('extrasmall_daybh',400,9624,1095,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='extrasmall_rankbh') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('extrasmall_rankbh',400,9624,1095,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='extrasmall_plain') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('extrasmall_plain',90,384,250,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='extrasmall_count') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('extrasmall_count',30,744,90,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='small_raw') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('small_raw',30,384,90,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='small_day') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('small_day',400,4824,1095,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='small_daybh') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('small_daybh',400,4824,1095,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='small_rankbh') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('small_rankbh',400,4824,1095,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='small_plain') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('small_plain',90,384,250,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='small_count') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('small_count',30,384,90,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='medium_raw') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('medium_raw',30,168,90,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='medium_day') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('medium_day',400,2160,1095,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='medium_daybh') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('medium_daybh',400,2160,1095,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='medium_rankbh') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('medium_rankbh',400,2160,1095,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='medium_plain') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('medium_plain',90,384,250,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='medium_count') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('medium_count',30,168,90,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='large_raw') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('large_raw',30,168,90,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='large_day') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('large_day',400,2160,1095,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='large_daybh') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('large_daybh',400,2160,1095,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='large_rankbh') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('large_rankbh',400,2160,1095,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='large_plain') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('large_plain',45,168,120,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='large_count') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('large_count',30,168,90,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='extralarge_raw') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('extralarge_raw',14,48,30,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='extralarge_day') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('extralarge_day',90,744,400,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='extralarge_daybh') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('extralarge_daybh',90,744,400,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='extralarge_rankbh') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('extralarge_rankbh',90,744,400,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='extralarge_plain') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('extralarge_plain',14,48,30,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='extralarge_count') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('extralarge_count',14,48,30,0)
end if;

--Partitions for ENIQ Events (VOLUME BASED)
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgeh_raw') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgeh_raw',109910304,25000000,329730912,1)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgeh_raw_lev2') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgeh_raw_lev2',989192736,250000000,2967578208,1)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehextralarge_raw') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehextralarge_raw',549551520,40000000,1648654560,1)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehlarge_raw') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehlarge_raw',549551520,80000000,1648654560,1)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehmedium_raw') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehmedium_raw',54955152,40000000,164865456,1)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehsmall_raw') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehsmall_raw',60000000,10000000,120000000,1)
end if;

--Partitions for ENIQ Events (TIME BASED)
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgeh_1min') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgeh_1min',7,168,14,0)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgeh_15min') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgeh_15min',7,168,14,0)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgeh_day') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgeh_day',4320,4320,1095,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehlarge_1min') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehlarge_1min',7,48,14,0)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehlarge_15min') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehlarge_15min',7,168,14,0)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehlarge_day') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehlarge_day',15,336,30,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehmedium_1min') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehmedium_1min',7,168,14,0)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehmedium_15min') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehmedium_15min',7,168,14,0)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehmedium_day') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehmedium_day',15,168,30,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehsmall_1min') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehsmall_1min',7,168,14,0)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehsmall_15min') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehsmall_15min',7,168,14,0)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehsmall_day') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehsmall_day',15,336,30,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehextralarge_1min') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehextralarge_1min',7,168,14,0)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehextralarge_15min') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehextralarge_15min',30,336,90,0)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehextralarge_day') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehextralarge_day',400,2160,1095,0)
end if;

if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehextrasmall_1min') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehextrasmall_1min',7,168,14,0)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehextrasmall_15min') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehextrasmall_15min',15,168,30,0)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='sgehextrasmall_day') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('sgehextrasmall_day',400,2160,1095,0)
end if;

--Partitions for NetAn (TIME BASED)
if not exists (select 1 from PartitionPlan_temp where partitionplan='cv_day') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('cv_day',400,1800,1095,0)
end if;
if not exists (select 1 from PartitionPlan_temp where partitionplan='cv_raw') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('cv_raw',30,168,90,0)
end if;

--Partitions for ENIQ Stats (BULK CM)
if not exists (select 1 from PartitionPlan_temp where partitionplan='bulk_cm_raw') then
  insert into PartitionPlan_temp (partitionplan,defaultstoragetime,defaultpartitionsize,maxstoragetime,partitiontype) values ('bulk_cm_raw',400,4824,1095,0)
end if;