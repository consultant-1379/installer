CREATE OR REPLACE PROC dwhrep.DWHMONITOR_DWHREP
(
	IN @TECHPACK_NAME varchar(256)
)

BEGIN

delete from DefaultTags where DATAFORMATID like @TECHPACK_NAME || ':((%))%';
delete from DataItem where DATAFORMATID like @TECHPACK_NAME || ':((%))%';
delete from InterfaceMeasurement where DATAFORMATID like @TECHPACK_NAME || ':((%))%';
delete from DataFormat where DATAFORMATID like @TECHPACK_NAME || ':((%))%';
delete from MeasurementColumn where MTABLEID like @TECHPACK_NAME || ':((%))%';
delete from MeasurementVector where TYPEID like @TECHPACK_NAME || ':((%))%';
delete from MeasurementCounter where TYPEID like @TECHPACK_NAME || ':((%))%';
delete from MeasurementKey where TYPEID like @TECHPACK_NAME || ':((%))%';
delete from AggregationRule where VERSIONID like @TECHPACK_NAME || ':((%))%';
delete from MeasurementTable where MTABLEID like @TECHPACK_NAME || ':((%))%';
delete from BusyhourMapping where TYPEID like @TECHPACK_NAME || ':((%))%';
delete from MeasurementObjBHSupport where TYPEID like @TECHPACK_NAME || ':((%))%';
delete from MeasurementDeltaCalcSupport where TYPEID like @TECHPACK_NAME || ':((%))%';
delete from MeasurementType where TYPEID like @TECHPACK_NAME || ':((%))%';
delete from MeasurementTypeClass where VERSIONID like @TECHPACK_NAME || ':((%))%';
delete from ReferenceColumn where TYPEID like @TECHPACK_NAME || ':((%))%';
delete from ReferenceTable where TYPEID like @TECHPACK_NAME || ':((%))%';
delete from Transformation where TRANSFORMERID like @TECHPACK_NAME || ':((%))%';
delete from Transformer where TRANSFORMERID like @TECHPACK_NAME || ':((%))%';
delete from UniverseCondition where VERSIONID like @TECHPACK_NAME || ':((%))%';
delete from UniverseObject where VERSIONID like @TECHPACK_NAME || ':((%))%';
delete from UniverseJoin where VERSIONID like @TECHPACK_NAME || ':((%))%';
delete from UniverseName where VERSIONID like @TECHPACK_NAME || ':((%))%';
delete from UniverseTable where VERSIONID like @TECHPACK_NAME || ':((%))%';
delete from UniverseClass where VERSIONID like @TECHPACK_NAME || ':((%))%';
delete from UniverseFormulas where VERSIONID like @TECHPACK_NAME || ':((%))%';
delete from BusyhourPlaceholders where VERSIONID like @TECHPACK_NAME || ':((%))%';
delete from ExternalStatement where VERSIONID like @TECHPACK_NAME || ':((%))%';
delete from VerificationCondition where VERSIONID like @TECHPACK_NAME || ':((%))%';
delete from VerificationObject where VERSIONID like @TECHPACK_NAME || ':((%))%';
delete from SupportedVendorRelease where VERSIONID like @TECHPACK_NAME || ':((%))%';
delete from BusyhourRankkeys where VERSIONID like @TECHPACK_NAME || ':((%))%';
delete from BusyhourSource where VERSIONID like @TECHPACK_NAME || ':((%))%';
delete from Busyhour where VERSIONID like @TECHPACK_NAME || ':((%))%';
delete from Aggregation where VERSIONID like @TECHPACK_NAME || ':((%))%';
delete from TechPackDependency where VERSIONID like @TECHPACK_NAME || ':((%))%';
delete from TypeActivation where TECHPACK_NAME=@TECHPACK_NAME;
delete from Versioning where VERSIONID like @TECHPACK_NAME || ':((%))%';
delete from DWHColumn where STORAGEID in (select STORAGEID from  DWHType where TECHPACK_NAME=@TECHPACK_NAME);
delete from DWHPartition where STORAGEID in (select STORAGEID from  DWHType where TECHPACK_NAME=@TECHPACK_NAME);
delete from DWHType where TECHPACK_NAME=@TECHPACK_NAME;
delete from ExternalStatementStatus where VERSIONID like @TECHPACK_NAME || ':((%))%';
delete from DWHTechpacks where VERSIONID like @TECHPACK_NAME || ':((%))%';
delete from TPActivation where VERSIONID like @TECHPACK_NAME || ':((%))%';

END;