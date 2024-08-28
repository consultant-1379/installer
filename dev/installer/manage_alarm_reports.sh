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
#
# ********************************************************************
# Name    : store_to_db.sh
# Date    : 08/06/2016
# Purpose : Script to disable alarm reports pre-upgrade and enable alarm reports post-upgrade
#           
# Usage   : manage_alarm_reports.sh disable|enable
#
# ********************************************************************
#
# Command Section
#
# ********************************************************************

AWK=/usr/bin/awk
CAT=/usr/bin/cat
DATE=/usr/bin/date
ECHO=/usr/bin/echo
GREP=/usr/bin/grep
RM=/usr/bin/rm
CONF_DIR=/eniq/sw/conf
INSTALLER_DIR=/eniq/sw/installer
ENIQ_CONFIG_DIR=/eniq/installation/config
BIN_DIR=/eniq/sw/bin/

if [ -s /eniq/admin/lib/common_functions.lib ]; then
    . /eniq/admin/lib/common_functions.lib
else
    $ECHO "Could not find /eniq/admin/lib/common_functions.lib"
    exit 1
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

DWHREPusername=`inigetpassword REP -v DWHREPUsername -f ${CONF_DIR}/niq.ini`
DWHREPPassword=`inigetpassword REP -v DWHREPPassword -f ${CONF_DIR}/niq.ini`
CURR_SERVER_TYPE=`cat $ENIQ_CONFIG_DIR/installed_server_type | grep -v  '^[[:blank:]]*#' | sed -e 's/ //g'`
connection_string=/var/tmp/encrypt_$$.txt
connection_string_decrypt="-nogui -c \"eng=repdb;links=tcpip{host=repdb;port=2641};uid=${DWHREPusername};pwd=${DWHREPPassword}\"" 

get_encrypt_file "${connection_string_decrypt}" "${connection_string}"

. /eniq/sybase_iq/IQ-*/IQ-*.sh
 
sybase_env_variables_ec=$?
if [ $sybase_env_variables_ec -ne 0 ]; then
     $ECHO "Unable to find sybase env variables"
fi

DBISQL=$(which dbisql)

if [  "${CURR_SERVER_TYPE}" != "eniq_stats" ] ; then
	$ECHO "not a stats server exiting manage_alarm_reports.sh..."
	exit 0
fi

# ********************************************************************
#
# 	Functions
#
# ********************************************************************
### Function: get_active_reports###
# Arguments:
#       none
# Return Values:
#       none

get_active_reports()
{
	if [ -s $INSTALLER_DIR/tmp_alarm_report.txt ]; then
		$ECHO "Active alarm reports already fetched...exiting the script.."
		
		exit 1
	fi

	${DBISQL} @${connection_string} "select REPORTID from AlarmReport where STATUS='ACTIVE';OUTPUT TO ${INSTALLER_DIR}/tmp_alarm_report.txt" >/dev/null
	if [ $? -ne "0" ] ; then
		_err_msg_="ERROR: Could not get active alarm reports"
		abort_script "${_err_msg_}"
		
		exit "${E}"
	elif [ ! -s $INSTALLER_DIR/tmp_alarm_report.txt ]; then
		$ECHO "No Alarm Reports found..exiting script..."
		exit 0
	else
		$ECHO "Active alarm reports fetched successfully"
	fi

	reports disable
	if [ $? -ne "0" ] ; then
		_err_msg_="ERROR: Could not disable active alarm reports"
		abort_script "${_err_msg_}"
		exit "${E}"
	else
		$ECHO "Alarm reports disabled successfully"
	fi
}

# ********************************************************************
#
# 	Functions
#
# ********************************************************************
### Function: reset_reports_post_upgrade###
# Arguments:
#       none
# Return Values:
#       none

reset_reports_post_upgrade()
{
	if [ ! -s $INSTALLER_DIR/tmp_alarm_report.txt ]; then
		$ECHO "No Alarm Reports backup found..exiting script..."
		exit 0
	fi
	reports enable
	if [ $? -ne "0" ] ; then
		_err_msg_="ERROR: Could not reset alarm reports post upgrade"
		abort_script "${_err_msg_}"
	else
		$ECHO "Alarm reports reset done successfully"
	fi
	$RM -rf ${INSTALLER_DIR}/tmp_alarm_report.txt
	
}

# ********************************************************************
#
# 	Functions
#
# ********************************************************************
### Function: reports ###
# Arguments:
#       $1 - enable|disable
# Return Values:
#       0 : Success
#	 >0 : Failure

reports()
{
	if [[ "${1}" == "enable" ]] ; then
		while read p; do
		echo "update AlarmReport set STATUS='ACTIVE' where REPORTID=$p" >>${INSTALLER_DIR}/temp_alarm_reports.sql
		
		done <${INSTALLER_DIR}/tmp_alarm_report.txt
		if [ $? -ne 0 ] ; then
			_err_msg_="Error: File not found" 
			abort_script "${_err_msg_}"
		fi

		${DBISQL} @${connection_string} /eniq/sw/installer/temp_alarm_reports.sql >/dev/null
		if [ $? -ne 0 ] ; then
			_err_msg_="Error when connecting to database: Could not enable inactive alarm reports" 
			abort_script "${_err_msg_}"
		fi
		
	elif [[ "${1}" == "disable" ]] ; then
		while read p; do
		echo "update AlarmReport set STATUS='INACTIVE' where REPORTID=$p" >>${INSTALLER_DIR}/temp_alarm_reports.sql
		
		done <${INSTALLER_DIR}/tmp_alarm_report.txt
		${DBISQL} @${connection_string} /eniq/sw/installer/temp_alarm_reports.sql >/dev/null
		if [ $? -ne 0 ] ; then
			_err_msg_="Error when connecting to database: Could not disable active alarm reports" 
			abort_script "${_err_msg_}"
		fi
	else
		$ECHO "give correct input"
	fi
	$RM -rf ${INSTALLER_DIR}/temp_alarm_reports.sql

	${BIN_DIR}/engine -e reloadAlarmCache
	if [ $? -ne 0 ] ; then
		$ECHO "Failed to reload the alarm cache"
	fi

}

# ********************************************************************
#
# 	Functions
#
# ********************************************************************
### Function: abort_script ###
#
#   This will is called if the script is aborted through an error
#   signal sent by the kernel such as CTRL-C or if a serious
#   error is encountered during runtime
#
# Arguments:
#       $1 - Error message from part of program (Not always used)
# Return Values:
#       none

abort_script()
{
_err_time_=`$DATE '+%Y-%b-%d_%H.%M.%S'`

if [ "$1" ]; then
    _err_msg_="${_err_time_} - $1"
else
    _err_msg_="${_err_time_} - ERROR : Script aborted.......\n"
fi

if [ "${LOGFILE}" ]; then
    $ECHO "\nERROR : ${_err_msg_}\n" | $TEE -a ${LOGFILE}
else
    $ECHO "\nERROR : ${_err_msg_}\n"
fi

cd $SCRIPTHOME
$RM -rf ${TEM_DIR}
exit 1
}

### Function: get_absolute_path ###
#
# Determine absolute path to software
#
# Arguments:
#	none
# Return Values:
#	none

get_absolute_path() 
{
_dir_=`$DIRNAME $0`
SCRIPTHOME=`cd $_dir_ 2>/dev/null && pwd || $ECHO $_dir_`
}

# ********************************************************************
#
# 	Main body of program
#
# ********************************************************************
#
case "$1" in
disable)
	get_active_reports
	;;

enable)
	reset_reports_post_upgrade
	;;
*)
     $ECHO "Usage: chnge_alarmReports_state.sh disable|enable"
     exit 10
     ;;
esac
