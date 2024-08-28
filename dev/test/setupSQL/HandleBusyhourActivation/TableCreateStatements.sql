/*==============================================================*/
/* Table: Busyhour                                              */
/*==============================================================*/
create table Busyhour (
    VERSIONID		varchar(128),
    BHLEVEL		varchar(32),
    BHTYPE		varchar(32),   
    BHCRITERIA		varchar(32000),
    WHERECLAUSE		varchar(32000),
    DESCRIPTION		varchar(32000),
    TARGETVERSIONID	varchar(128),  
    BHOBJECT		varchar(32),   
    BHELEMENT		integer,   
    ENABLE		integer,   
    AGGREGATIONTYPE	varchar(128),  
    OFFSET		integer,   
    WINDOWSIZE		integer,   
    LOOKBACK		integer,   
    P_THRESHOLD		integer,   
    N_THRESHOLD		integer,   
    CLAUSE		varchar(32000),
    PLACEHOLDERTYPE	varchar(128),  
    GROUPING		varchar(32),   
    REACTIVATEVIEWS	integer
);                      
                        
/*==============================================================*/
/* Table: BusyhourPlaceholders                                  */
/*==============================================================*/
create table BusyhourPlaceholders (
    VERSIONID               varchar(128),
    BHLEVEL		    varchar(32),
    PRODUCTPLACEHOLDERS	    integer,
    CUSTOMPLACEHOLDERS	    integer
);

/*==============================================================*/
/* Table: TPActivation	                                        */
/*==============================================================*/
create table TPActivation (
TECHPACK_NAME varchar(30),
STATUS        varchar(10), 
VERSIONID     varchar(128),
TYPE          varchar(10), 
MODIFIED      integer
); 

/*==============================================================*/
/* Table: BusyhourMapping                                       */
/*==============================================================*/
create table BusyhourMapping (
VERSIONID        varchar(128),
BHLEVEL          varchar(32),
BHTYPE           varchar(32), 
TARGETVERSIONID  varchar(128),
BHOBJECT         varchar(32), 
TYPEID           varchar(255),
BHTARGETTYPE     varchar(128),
BHTARGETLEVEL    varchar(128),
ENABLE           integer
);

/*==============================================================*/
/* Table: BusyhourRankkeys                                      */
/*==============================================================*/
create table BusyhourRankkeys (
VERSIONID        varchar(128),
BHLEVEL          varchar(32), 
BHTYPE           varchar(32), 
KEYNAME          varchar(128),
KEYVALUE         varchar(128),
ORDERNBR         numeric(9),  
TARGETVERSIONID  varchar(128),
BHOBJECT         varchar(32)
);


/*==============================================================*/
/* Table: BusyhourSource                                        */
/*==============================================================*/
create table BusyhourSource (
VERSIONID        varchar(128),
BHLEVEL          varchar(32), 
BHTYPE           varchar(32), 
TYPENAME         varchar(255),
TARGETVERSIONID  varchar(128),
BHOBJECT         varchar(32)
);

/*==============================================================*/
/* Table: Aggregation                                           */
/*==============================================================*/
create table Aggregation (
AGGREGATION        varchar(255),
VERSIONID          varchar(128),
AGGREGATIONSET     varchar(100),
AGGREGATIONGROUP   varchar(100),
REAGGREGATIONSET   varchar(100),
REAGGREGATIONGROUP varchar(100),
GROUPORDER         integer,   
AGGREGATIONORDER   integer,    
AGGREGATIONTYPE    varchar(50), 
AGGREGATIONSCOPE   varchar(50)
); 
                         
/*==============================================================*/
/* Table: AggregationRule                                       */
/*==============================================================*/
create table AggregationRule (
AGGREGATION           varchar(255),                   
VERSIONID             varchar(128),
RULEID                integer, 
TARGET_TYPE           varchar(50), 
TARGET_LEVEL          varchar(50), 
TARGET_TABLE          varchar(255),
TARGET_MTABLEID       varchar(255),
SOURCE_TYPE           varchar(50), 
SOURCE_LEVEL          varchar(50), 
SOURCE_TABLE          varchar(255),
SOURCE_MTABLEID       varchar(255),
RULETYPE              varchar(50), 
AGGREGATIONSCOPE      varchar(50), 
BHTYPE                varchar(50), 
ENABLE                integer
);
