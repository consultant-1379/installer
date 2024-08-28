#!/bin/bash
# ********************************************************************
# Ericsson Radio Systems AB                                     SCRIPT
# ********************************************************************
#
# (c) Ericsson Radio Systems AB 2016 - All rights reserved.
#
# The copyright to the computer program(s) herein is the property
# of Ericsson Radio Systems AB, Sweden. The programs may be used
# and/or copied only with the written permission from Ericsson Radio
# Systems AB or in accordance with the terms and conditions stipulated
# in the agreement/contract under which the program(s) have been
# supplied.
#
# ********************************************************************
# Name    : retrieve_from_db.sh
# Date    : 18/05/2016
# Purpose : Script to retrieve the alarm password from database and store
#			into a temporary file in /eniq/sw/installer/temp_db_result/
#           
# Usage   : retrieve_from_db.sh
#
# ********************************************************************
#
# Command Section
#
# ********************************************************************

INSTALLER_DIR=/eniq/sw/installer
CONF_DIR=/eniq/sw/conf
TEMP_DIR=$INSTALLER_DIR/temp_db_result

if [ ! -r "${BIN_DIR}/common_variables.lib" ] ; then
  echo "ERROR: Source file is not readable at ${BIN_DIR}/common_variables.lib"
  exit 3
fi

. ${BIN_DIR}/common_variables.lib

# Source the common functions
if [ -s /eniq/admin/lib/common_functions.lib ]; then
    . /eniq/admin/lib/common_functions.lib
else
  $ECHO "File ${ENIQ_BASE_DIR}/admin/lib/common_functions.lib not found"
  exit 4
fi

########################################################################
# Function: remove_connection_string
# Removes/Deletes connection string once the script terminates
#
# Arguments: None
#
# Return Values: None
remove_connection_string()
{
if [ -f $connection_string ]; then
  $RM -f $connection_string
  if [ $? != 0 ]; then
    $ECHO "Unable to delete $connection_string"
  fi
fi
}

trap remove_connection_string EXIT

ETLREPUsername=`inigetpassword REP -v ETLREPUsername -f ${CONF_DIR}/niq.ini`
ETLREPPassword=`inigetpassword REP -v ETLREPPassword -f ${CONF_DIR}/niq.ini`

connection_string=/var/tmp/encrypt_$$.txt
connection_string_decrypt="-c \"eng=repdb;links=tcpip{host=repdb;port=2641};uid=${ETLREPUsername};pwd=${ETLREPPassword}\" -onerror exit -nogui"

get_encrypt_file "${connection_string_decrypt}" "${connection_string}"

. /eniq/sybase_iq/IQ-*/IQ-*.sh
 
sybase_env_variables_ec=$?
if [ $sybase_env_variables_ec -ne 0 ]; then
     $ECHO "Unable to find sybase env variables"
fi

DBISQL=$(which dbisql)

if [ -s $INSTALLER_DIR/temp_db_result/tmp.txt ]; then
	$ECHO "Alarm password backup already taken"
	exit 0
else
	_retrived_data=`${DBISQL} @${connection_string} "select ACTION_CONTENTS_01 from META_TRANSFER_ACTIONS WHERE ACTION_TYPE = 'alarmhandler' and ENABLED_FLAG = 'Y'"`
	_dbErrCodedbErrCode=$?
	if [[ $_dbErrCode -ne 0 ]]; then
		$ECHO "Could not retrieve alarm password from Database - exiting with error script: $dbErrCode"
		exit $_dbErrCode
	else
		_alarm_password=`$ECHO "$_retrived_data" | $GREP password` 
		if [[ -z $_alarm_password ]]; then
			$ECHO "No alarm password found in the database..exiting script"
			exit 0
		else
			if [ ! -d $TEMP_DIR ];then
				$MKDIR $TEMP_DIR
			fi
			$ECHO "$_alarm_password" | $NAWK -F'=' 'NR==1 { print $2 } '> $TEMP_DIR/tmp.txt
			$ECHO "Alarm password backed up successfully."
		fi
	fi	
fi