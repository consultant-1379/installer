#!/usr/bin/sh
# ********************************************************************
# Ericsson Radio Systems AB                                     SCRIPT
# ********************************************************************
#
#
# (c) Ericsson Radio Systems AB 2018 - All rights reserved.
#
# The copyright to the computer program(s) herein is the property
# of Ericsson Radio Systems AB, Sweden. The programs may be used 
# and/or copied only with the written permission from Ericsson Radio 
# Systems AB or in accordance with the terms and conditions stipulated 
# in the agreement/contract under which the program(s) have been 
# supplied.
#
# ********************************************************************
# Name    : data_delete.sh
# Date    : 15/07/2020(dummy date) Last modified 10/05/2023
# Purpose : Ericsson Network IQ Data deletion script
# Usage   : Migrate Data from the Raw to newly implemented Count table for any Techpack
# Author  : ENIQ-Statistic Design Team
# ********************************************************************

if [ ! -r "${BIN_DIR}/common_variables.lib" ] ; then
  echo "ERROR: Source file is not readable at ${BIN_DIR}/common_variables.lib"
  exit 3
fi

. ${BIN_DIR}/common_variables.lib

### Function: usage_msg ###
#
#   Print out the usage message
#
# Arguments:
#	none
# Return Values:
#	none

usage_msg() 
{
$ECHO ""
$ECHO "Usage: data_delete -t <TP_NAME>"
$ECHO "options:"
$ECHO "-t  : Option needed for TP Name"
}

####Global_Variables used########

DC_USER=dc
DWHREP_USER=dwhrep
DWH_NAME_dwhdb=dwhdb
DWH_NAME_repdb=repdb
SYBASE_IQ_PATH=/eniq/sybase_iq/OCS-16_0/bin/iqisql
log_path=/eniq/sw/installer
file_creation=/eniq/sw/installer



######## Main Execution ##########

if [ $# != 2 ]
then
	usage_msg
        exit
fi

if [ $1 != "-t" ]
then
	usage_msg
        exit
fi
tpName=$2

${SYBASE_IQ_PATH} -U${DWHREP_USER} -P${DWHREP_USER} -S${DWH_NAME_repdb} -b << EOISQL > output.txt
select * into temp from DWHPARTITION where tablename like '${tpName}%'
go
unload table temp to '${file_creation}/truncate_techpack.txt' QUOTES OFF
go
drop table temp
go
EOISQL

$CD ${file_creation}

$NAWK -F,  '{print "truncate table " $2}' ${file_creation}/truncate_techpack.txt > ${file_creation}/result.sql

$ECHO "go" >> ${file_creation}/result.sql

$ECHO "Please wait sql queries are running..."
${SYBASE_IQ_PATH}  -P${DC_USER} -U${DC_USER} -S${DWH_NAME_dwhdb} -i ${file_creation}/result.sql -o ${file_creation}/query.output

$ECHO "SQL Statements executed successfully"
#$RM ${file_creation}/dwhpartition_data.txt
#$RM ${file_creation}/result.sql
$RM ${file_creation}/query.sql > /dev/null 2>&1
$RM ${file_creation}/vivek.txt > /dev/null 2>&1
#$RM ${file_creation}/query.output
