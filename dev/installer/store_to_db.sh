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
# Date    : 18/05/2016
# Purpose : Script to store the retrieved password to databse
#           
# Usage   : store_to_db.sh
#
# ********************************************************************
#
# Command Section
#
# ********************************************************************

INSTALLER_DIR=/eniq/sw/installer
ENIQ_CONFIG_DIR=/eniq/installation/config
TMP=${INSTALLER_DIR}/temp_db_result
RM=/usr/bin/rm
CD=/usr/bin/cd


CURR_SERVER_TYPE=`cat $ENIQ_CONFIG_DIR/installed_server_type | grep -v  '^[[:blank:]]*#' | sed -e 's/ //g'`
if [  "${CURR_SERVER_TYPE}" == "eniq_stats" ] ; then
	if [ -d $INSTALLER_DIR/temp_db_result ]; then
		pswd=$(<$TMP/tmp.txt)
		cd /eniq/sw/bin
		./change_alarm_password.bsh -p $pswd >/dev/null
		 $RM -rf $TMP
	else
		$ECHO "Password backup not found..cannot restore Alarm Password"
		exit 0
	fi
else
	$ECHO "Not a stats server exiting store_to_db.sh..."
	exit 0
fi




