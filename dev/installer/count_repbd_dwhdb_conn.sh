#!/usr/bin/bash
# ********************************************************************
# Ericsson Radio Systems AB                                     SCRIPT
# ********************************************************************
#
# (c) Ericsson Radio Systems AB 2013 - All rights reserved.
#
# The copyright to the computer program(s) herein is the property
# of Ericsson Radio Systems AB, Sweden. The programs may be used 
# and/or copied only with the written permission from Ericsson Radio 
# Systems AB or in accordance with the terms and conditions stipulated 
# in the agreement/contract under which the program(s) have been 
# supplied.
#
# ********************************************************************
# Name    : count_repbd_dwhdb_conn.sh
# Revision: main/at_eniq/1
# Purpose : This script will count the most recently-prepared SQL  
# statement for each connection to the specified database on the server. 
# The script will save data for each min.
#
# Usage   : count_repbd_dwhdb_conn.sh -h <hours>
#
# ********************************************************************
# Subrata Bhowmick (subrata.bhowmick@wipro.com)
# ********************************************************************

NAWK=/usr/bin/nawk

if [ -s /eniq/admin/lib/common_functions.lib ]; then
    . /eniq/admin/lib/common_functions.lib
else
    $ECHO "Could not find /eniq/admin/lib/common_functions.lib"
    exit 1
fi

if [ "$#" != "2" ] ; then
  echo "Usage : ./count_repbd_dwhdb_conn.sh -h 3" 
  exit 32
fi

while true; do     
    case $1 in
		-h)
			TOTALHOUR=$2; break
		;;
		*)	
			echo "Usage : ./count_repbd_dwhdb_conn.sh -h 3" 
			echo "Please use any hour in place of 3"
			exit 32
		;;
	esac
done

#
# check we are dcuser...
#
if [ ${LOGNAME} != "dcuser" ] ; then
    echo ""
    echo "This script has to be run as 'dcuser'. You are currently logged in as '${LOGNAME}'"
    echo "Migration script aborting..."
    exit 32
fi

#
# log file name
#
HOSTNAME=`hostname`
LOGFILE="serverspeed"$HOSTNAME".log"

#
# check sql out-put
#
check_sqlout() {

SQLOUTFILE=$1

ERRORMSG=`cat $SQLOUTFILE | grep -v "already exists" | egrep -i "command not found|SQL Anywhere Error|CT-LIBRARY error"`
ERRORCODE=`cat $SQLOUTFILE | grep -v "already exists" | egrep -i "command not found|SQL Anywhere Error|CT-LIBRARY error" | wc -l | $NAWK '{print $1}'`

if [ "$ERRORCODE" != 0 ]; then
   echo "Error ! $ERRORMSG"
   echo "For more error details please check the log file : $LOGFILE"
   cat "Error ! " >> ${LOGFILE}
   cat $SQLOUTFILE >> ${LOGFILE}
   exit 32
fi
}

# ********************************************************************
#
#   Main body of program
#
# ********************************************************************
#
echo "Log file : $LOGFILE " 2>&1 > ${LOGFILE}

CONF_DIR=/eniq/sw/conf
export CONF_DIR

DBAPASSWORD=`inigetpassword DB -f ${CONF_DIR}/niq.ini -v DBAPassword`

# create temporary table to keep record 
# - if required table "dwh_repdb_count" already present it will not create again
# - if the table is not present but script is not able to create the table it will throw an error and exit
iqisql -Udba -P$DBAPASSWORD -Srepdb -i create_rep_dwh_temp.sql > /tmp/tablecreated.txt 
check_sqlout /tmp/tablecreated.txt

# run for hours
echo "This script will continue for $TOTALHOUR hours." >> ${LOGFILE}

TOTALRUN=`expr 60 \* $TOTALHOUR`

START=1
while [ $START -le $TOTALRUN ]
do
	# check no of repdb connection at present
	iqisql -Udba -P$DBAPASSWORD -Srepdb -w1000 -i count_repdb_conn.sql -b | grep -v row > /tmp/insertdata.sql

	echo "go" >> /tmp/insertdata.sql
	check_sqlout /tmp/insertdata.sql
	
	# check no of dwhdb connection at present
	iqisql -Udba -P$DBAPASSWORD -Sdwhdb -w1000 -i count_dwhdb_conn.sql -b | grep -v row >> /tmp/insertdata.sql

	echo "go" >> /tmp/insertdata.sql
	check_sqlout /tmp/insertdata.sql
	
	# insert the data into temp table
	iqisql -Udba -P$DBAPASSWORD -Srepdb -i /tmp/insertdata.sql > /tmp/tableinserted.txt
	check_sqlout /tmp/tableinserted.txt
	
	START=$(( START+1 ))	 # increments $n
	sleep 60
done

echo "Script End." >> ${LOGFILE}
echo "Please chcek the table dwh_repdb_count in repdb to get the details." >> ${LOGFILE}