#!/bin/bash
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
# Name    : NetAnFileHandler.sh
# Date    : 15/07/2020(dummy date) Last modified 10/05/2023
# Purpose : Ericsson Network IQ Network Analytics parser file creation Script
# Usage   : ./NetAnFileHandler.sh
# Author  : XKUMDEY
# ********************************************************************
. /eniq/home/dcuser/.profile
. $HOME/.profile

. ${CONF_DIR}/niq.rc

if [ -z "${CONF_DIR}" ] ; then
  echo "ERROR: CONF_DIR is not set"
  exit 1
fi
if [ ! -r "${CONF_DIR}/niq.rc" ] ; then
  echo "ERROR: Source file is not readable at ${CONF_DIR}/niq.rc"
  exit 2
fi
. ${CONF_DIR}/niq.rc
if [ -z "${BIN_DIR}" ] ; then
  echo "ERROR: BIN_DIR is not set"
  exit 1
fi
if [ ! -r "${BIN_DIR}/common_variables.lib" ] ; then
  echo "ERROR: Source file is not readable at ${BIN_DIR}/common_variables.lib"
  exit 2
fi
. ${BIN_DIR}/common_variables.lib

NOW=$(${DATE} +"%Y-%m-%d")
LOG_DIR=/eniq/log/sw_log
LOG_FILE=$LOG_DIR/engine/NetAnFileHandler_$NOW.log


#$ECHO "`$DATE '+%Y_%m_%d_%H:%M:%S'` : $$ LogFile initialised" >> ${LOG_FILE}
##EQEV-103319. New Rack - manage_heap script calling dbisql resulting in high semaphore consumption
MAX_TIME=30
PIDFILE=/eniq/home/dcuser/.NetAnFileHandlerPid
#$ECHO $$ >> ${LOG_FILE}
if [[ ! -e $PIDFILE ]]; then
    $TOUCH $PIDFILE
fi
if [ ! -f $PIDFILE ]
then
  #$ECHO $$ > ${LOG_FILE}
  #$ECHO "`$DATE '+%Y_%m_%d_%H:%M:%S'` : Previous instance is running" >> ${LOG_FILE}
  if [ $? -ne 0 ]
  then
    $ECHO "`$DATE '+%Y_%m_%d_%H:%M:%S'` : Failed to create PID file. NetAnFileHandler script will not proceed." >> ${LOG_FILE}
    exit 1
  fi
else
  PID=`$CAT $PIDFILE`  
  for procid in $PID
  do
	 #$ECHO "`$DATE '+%Y_%m_%d_%H:%M:%S'` : $procid" >> ${LOG_FILE}
     $PS -p $procid > /dev/null
     if [ $? -eq 0 ]
     then
       psetime=`$PS -o etime= -p $procid|tr -d " "`
       if [ ! -z $psetime ]
       then
          #$ECHO "`$DATE '+%Y_%m_%d_%H:%M:%S'` : $psetime and $procid" >> ${LOG_FILE}
          psmin=$($ECHO $psetime|awk -F":" '{print $1}')
          psminint=$(($psmin+0))
          if [ $psminint -ge $MAX_TIME ]
          then
             $ECHO "`$DATE '+%Y_%m_%d_%H:%M:%S'` : Previous process is already running with PID $procid for more than $MAX_TIME minutes. Killing the previous process with PID $procid" >> ${LOG_FILE}
             $KILL -9 $procid > /dev/null
             $ECHO "$($GREP -v $procid $PIDFILE)" > $PIDFILE
	      else
		     $ECHO "`$DATE '+%Y_%m_%d_%H:%M:%S'` : Previous process is already running with PID $procid for less than $MAX_TIME minutes. Exiting the current instance with PID $$" >> ${LOG_FILE}
             $ECHO "$($GREP -v $$ $PIDFILE)" > $PIDFILE
			 exit 1
          fi
       fi
     else
        $ECHO "$($GREP -v $procid $PIDFILE)" > $PIDFILE
        perl -i -n -e "print if /\S/" $PIDFILE
     fi
  done
  $ECHO $$ >> $PIDFILE
  if [ $? -ne 0 ]
  then
    $ECHO "`$DATE '+%Y_%m_%d_%H:%M:%S'` : Could not create PID file" >> ${LOG_FILE}
    exit 1
  fi
fi



for OSS_ID in `ls ${PMDATA_DIR} | grep -i eniq_oss_`
do
        echo ${OSS_ID}
	dir=/eniq/data/pmdata/${OSS_ID}/NetworkAnalytics
	find "$dir" -depth -type d 2>/dev/null |
		while read sub; do
		# case "$sub" in   */*) ;;   *) continue ;;   esac  # sub-dir only
		[ "`cd "$sub"; echo .* * ?`" = ". .. * ?" ] || continue
		now="$(date +'%d_%m_%Y-%T')"
		echo ${now} > "$sub/${now}.txt"
	done
	
	##Create input files for parsing 
	dir=/eniq/data/pmdata/${OSS_ID}/InformationStore 
	find "$dir" -depth -type d 2>/dev/null |
		while read sub; do
		# case "$sub" in   */*) ;;   *) continue ;;   esac  # sub-dir only
		[ "`cd "$sub"; echo .* * ?`" = ". .. * ?" ] || continue
		now="$(date +'%d_%m_%Y-%T')"
		echo ${now} > "$sub/${now}.txt"
	done
	
done
exit 0