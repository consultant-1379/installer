CREATE OR REPLACE PROC etlrep.DWHMONITOR_ETLREP
(
	IN @TECHPACK_NAME varchar(256)
)

BEGIN

delete from META_TRANSFER_BATCHES where COLLECTION_SET_ID in ( select COLLECTION_SET_ID from META_COLLECTION_SETS where COLLECTION_SET_NAME=@TECHPACK_NAME );
delete from META_TRANSFER_ACTIONS where COLLECTION_SET_ID in ( select COLLECTION_SET_ID from META_COLLECTION_SETS where COLLECTION_SET_NAME=@TECHPACK_NAME );
delete from META_SCHEDULINGS where COLLECTION_SET_ID in ( select COLLECTION_SET_ID from META_COLLECTION_SETS where COLLECTION_SET_NAME=@TECHPACK_NAME );
delete from META_COLLECTIONS where COLLECTION_SET_ID in ( select COLLECTION_SET_ID from META_COLLECTION_SETS where COLLECTION_SET_NAME=@TECHPACK_NAME );
delete from META_COLLECTION_SETS where COLLECTION_SET_NAME=@TECHPACK_NAME;

END;