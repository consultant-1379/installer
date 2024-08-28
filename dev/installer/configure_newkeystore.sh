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
# Name    : configure_newkeystore.sh
# Date    : 15/07/2020(dummy date) Last modified 10/05/2023
# Purpose : Ericsson Network IQ Runtime installation script
# Usage   : Configure_newkeystore.sh [-v] [-e]
# ********************************************************************

############## THE SCRIPT BEGINS HERE ##############

CONF_DIR=/eniq/sw/conf

if [ -s /eniq/admin/lib/common_functions.lib ]; then
    . /eniq/admin/lib/common_functions.lib
else
    echo "Could not find /eniq/admin/lib/common_functions.lib"
    exit 1
fi

if [ -s ${CONF_DIR}/niq.rc ]; then
    . ${CONF_DIR}/niq.rc
else
        echo "Could not find ${CONF_DIR}/niq.rc"
        exit 1
fi

if [ -z "${BIN_DIR}" ] ; then
  echo "ERROR: BIN_DIR is not set"
  exit 1
fi

if [ ! -r "${BIN_DIR}/common_variables.lib" ] ; then
  echo "ERROR: Source file is not readable at ${BIN_DIR}/common_variables.lib"
  exit 2
fi

. ${BIN_DIR}/common_variables.lib

VERBOSE=0
FORCE=0
CONFIGURED=0
LOG_DIR="/eniq/log/sw_log"
RT_DIR="/eniq/sw/runtime"
TIMESTAMP=`${DATE} +%d.%m.%y_%H:%M:%S`
HOST=/usr/bin/host
ENIQ_BASE_DIR="/eniq"
HOSTNAME=`${HOSTNAME}`
FULLNAME=`${ECHO} \`$HOST $HOSTNAME\` | $NAWK '{print $1;}'`
PRIVATEKEY=${RT_DIR}/tomcat/ssl/private/$HOSTNAME-private.pem
PUBLICKEY=${RT_DIR}/tomcat/ssl/${HOSTNAME}_public.key
CERTFILE=${RT_DIR}/tomcat/ssl/$HOSTNAME.cer
CSRFILE=${RT_DIR}/tomcat/ssl/$HOSTNAME.csr
OPENSSL=/usr/bin/openssl
P12KEYSTORE=${RT_DIR}/tomcat/ssl/keystore.pkcs12
JKEYSTORE=${RT_DIR}/tomcat/ssl/keystore.jks
KEYTOOL=${RT_DIR}/java/bin/keytool
HOSTOUTPUT=`${ECHO} \`$HOST $HOSTNAME\` | ${GREP} "has address"`
TRUSTSTORE=${RT_DIR}/jdk/jre/lib/security/truststore.ts
NIQ_INI=${CONF_DIR}/niq.ini
SERVER_XML=${RT_DIR}/tomcat/conf/server.xml
LIB=/eniq/admin/lib
HOME_DIR=/eniq/home/dcuser
DEFAULT_PLATFORM_INI=${CONF_DIR}/default_platform.ini

existingKeyStorePassFlag=`iniget DB -v keyStorePassValue_Encrypted -f $NIQ_INI`
existingKeyStorePass=`iniget KEYSTOREPASS -v keyStorePassValue -f $NIQ_INI`
isNiqUpdated="false"

isKeyStoreUpdated="false"

function _echo(){
	$ECHO ${*} | ${TEE} -a ${LOG_FILE}
}

#Encode Password
function encodePassword(){
	encryptedPass=""
	for i in $($SEQ 1 ${#1})
	do
		pass=`$PRINTF '%d\n' "'${1:i-1:1}"`
		encryptedPass=$encryptedPass"&#"$pass";"
	done
	
	$ECHO "$encryptedPass"
}

#Decode Password
function decodePassword(){
	decodedPass=""
	for value in `$ECHO $1 | $TR ';' '\n'`
	do
		char=`$ECHO $value | $CUT -d '#' -f2 | $AWK '{$PRINTF("%c",$1)}'`
		# Removing leading and trailing spaces
		char="$($ECHO -e "${char}" | $SED -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')"
		
		decodedPass=$decodedPass$char
	done
	$ECHO "$decodedPass"
}

#Reads and validates the new password
function validateNewPassword(){
	while :; do
	  unset Keystore_password
	  unset Keystore_password_reenter
	  _echo "Enter ${1} Password:"
      read -rs Keystore_password
	  _echo "Re-enter ${1} Password:"
	  read -rs Keystore_password_reenter
	
	  password_length=${#Keystore_password}
	
      if [ "$Keystore_password" != "$Keystore_password_reenter" ] ; then
		_echo "Mismatch in new password and re-entered new password. Please enter correctly."
		continue
	  elif  [ -z "${Keystore_password}" ] ; then
        _echo "The new password cannot be empty."
	    continue
	  elif [ ${password_length} -lt 6 -o ${password_length} -gt 30 ]; then
        _echo "The new password length should be not less than 6 characters or greater than 30 characters."
	    continue
      elif [[ "${Keystore_password}" =~ ^.*[\'\"\\\/]+.*$ ]] ; then
        _echo "The new password entered is not compliant with the Password Policy as it contains single quote ('), double quotes (\"), \\ or \/ characters."
	    continue
	  fi
	  break
	  
    done
}

#Update password in niq.ini and server.xml as dcuser
function modifyPassInFilesAsDcuser() {

	if [ ${LOGNAME} != "dcuser"  ] ; then
	  _echo "This option must be executed as dcuser"
	  exit 1
	fi
	
	if [ ! -d "${LOG_DIR}/SSL" ] ; then
	  ${MKDIR} ${LOG_DIR}/SSL
	fi

	LOG_FILE=${LOG_DIR}/SSL/keystore_${TIMESTAMP}.log
	$TOUCH $LOG_FILE
	
	iniset DB -f $NIQ_INI keyStorePassValue_Encrypted="Y"
	updated_pwd=`$ECHO $1 | $OPENSSL enc -base64`
	iniset KEYSTOREPASS -f $NIQ_INI keyStorePassValue=$updated_pwd
	if [ $? -ne 0 ] ; then
		_echo "Error in updating keystore password value in niq.ini file"
		exit 7
	else
		_echo "keystore password in niq.ini file updated successfully"
	fi
	
	password="$(encodePassword $1)"
	
	if [ -x /eniq/sw/bin/webserver ] ; then
	 
	 status=`/eniq/sw/bin/webserver status`
	 
	 if [[ $status == *OK* ]] ; then
	  _echo "Stopping Tomcat webserver ..."
	  /eniq/sw/bin/webserver stop
	 fi
    else
	  _echo "webserver command not found in /eniq/sw/bin directory."
	fi
	
	password=${password//"&"/"\&"}
	$SED -i "s/keystorePass=\".*\"/keystorePass=\"${password}\"/" $SERVER_XML
	if [ $? -ne 0 ] ; then
		_echo "Error in updating keystore password value in server.xml file"
		exit 7
	else
		_echo "keystore password in server.xml file updated successfully"
	fi
	
	_echo "Starting Tomcat ..." 
	if [ -x /eniq/sw/bin/webserver ] ; then
	  /eniq/sw/bin/webserver start
    else
	  _echo "Tomcat is not started as webserver command not found in /eniq/sw/bin directory."
	fi
}

#Update password in niq.ini and server.xml
function modifyPassInFiles() {
	
	isNiqUpdated="true"
	iniset DB -f $NIQ_INI keyStorePassValue_Encrypted="Y"
	updated_pwd=`$ECHO $1 | $OPENSSL enc -base64`
	iniset KEYSTOREPASS -f $NIQ_INI keyStorePassValue=$updated_pwd
	if [ $? -ne 0 ] ; then
		_echo "Error in updating keystore password value in niq.ini file"
		restore_configuration
		exit 7
	else
		_echo "keystore password in niq.ini file updated successfully"
	fi
	
	password="$(encodePassword $1)"
	
	if [ -x /eniq/sw/bin/webserver ] ; then
	 
	 status=`$SU - dcuser -c '/eniq/sw/bin/webserver status'`
	 
	 if [[ $status == *OK* ]] ; then
	  _echo "Stopping Tomcat webserver ..."
	  $SU - dcuser -c '/eniq/sw/bin/webserver stop'
	 fi
    else
	  _echo "webserver command not found in /eniq/sw/bin directory."
	fi
	
	if [ -f  "$SERVER_XML"_configureKeystoreBackupFile  ] ; then
	  ${RM} -f "$SERVER_XML"_configureKeystoreBackupFile
	fi
	$SU - dcuser -c "${CP} -f $SERVER_XML ${SERVER_XML}_configureKeystoreBackupFile"

	password=${password//"&"/"\&"}
	$SED -i "s/keystorePass=\".*\"/keystorePass=\"${password}\"/" $SERVER_XML
	if [ $? -ne 0 ] ; then
		_echo "Error in updating keystore password value in server.xml file"
		restore_configuration
		exit 7
	else
		_echo "keystore password in server.xml file updated successfully"
	fi
	
	_echo "Starting Tomcat ..." 
	if [ -x /eniq/sw/bin/webserver ] ; then
	  $SU - dcuser -c '/eniq/sw/bin/webserver start'
    else
	  _echo "Tomcat is not started as webserver command not found in /eniq/sw/bin directory."
	fi
}

function restore_configuration() {
	if [ "$isKeyStoreUpdated" == "true" -a -d ${RT_DIR}/tomcat/ssl/ssl_backup/ ] ; then
	  _echo "Restoring the $JKEYSTORE file."
	  $SU - dcuser -c "${MKDIR} -p ${RT_DIR}/tomcat/ssl_backup_configureKeystore_temp"
	  ${CHOWN} -R dcuser:dc5000 ${RT_DIR}/tomcat/ssl_backup_configureKeystore_temp
	  
	  $SU - dcuser -c "${CP} -rf ${RT_DIR}/tomcat/ssl/ssl_backup/* ${RT_DIR}/tomcat/ssl_backup_configureKeystore_temp"
	  
	  ${RM} -rf ${RT_DIR}/tomcat/ssl/*
	  $SU - dcuser -c "${CP} -rf ${RT_DIR}/tomcat/ssl_backup_configureKeystore_temp/*  ${RT_DIR}/tomcat/ssl/"
	  if [ $? -eq 0 ] ; then
	    _echo "$JKEYSTORE file successfully restored."
	  fi
	  
	  $SU - dcuser -c "${MKDIR} -p ${RT_DIR}/tomcat/ssl/ssl_backup/"
	  ${CHOWN} -R dcuser:dc5000 ${RT_DIR}/tomcat/ssl/ssl_backup/
	  $SU - dcuser -c "${CP} -rf ${RT_DIR}/tomcat/ssl_backup_configureKeystore_temp/* ${RT_DIR}/tomcat/ssl/ssl_backup/"
	  
	  if [ $? -eq 0 ] ; then
	    _echo "${RT_DIR}/tomcat/ssl/ssl_backup/ directory is successfully restored."
	    ${RM} -rf ${RT_DIR}/tomcat/ssl_backup_configureKeystore_temp
	  else
	    _echo "${RT_DIR}/tomcat/ssl/ssl_backup/ directory could not be created. Current backup will be found in ${RT_DIR}/tomcat/ssl_backup_configureKeystore_temp/"
	  fi
	fi
	
	if [ -f  "$TRUSTSTORE"_configureKeystoreBackupFile  ] ; then
	  _echo "Restoring the $TRUSTSTORE file."
	  ${RM} -f $TRUSTSTORE
	  $SU - dcuser -c "${CP} -f {$TRUSTSTORE}_configureKeystoreBackupFile $TRUSTSTORE"
	  
	  if [ $? -eq 0 ] ; then
	    _echo "$TRUSTSTORE file successfully restored."
	  fi
	fi

	if [ "$isNiqUpdated" == "true" ] ; then
	  _echo "Restoring the $NIQ_INI file."
	  iniset DB -f $NIQ_INI keyStorePassValue_Encrypted="$existingKeyStorePassFlag"
	  iniset KEYSTOREPASS -f $NIQ_INI keyStorePassValue=$existingKeyStorePass
	  
	  if [ $? -eq 0 ] ; then
	    _echo "$NIQ_INI file successfully restored."
	  fi
	fi
	
	if [ -f  "$SERVER_XML"_configureKeystoreBackupFile  ] ; then
	  _echo "Restoring the $SERVER_XML file."
	  ${RM} -f $SERVER_XML
	  $SU - dcuser -c "${CP} -f ${SERVER_XML}_configureKeystoreBackupFile $SERVER_XML"
	  
	  if [ $? -eq 0 ] ; then
	    _echo "$SERVER_XML file successfully restored."
	  fi
	fi
}

function cleanup() {

	if [ -f  "$TRUSTSTORE"_configureKeystoreBackupFile  ] ; then
	  ${RM} -f "$TRUSTSTORE"_configureKeystoreBackupFile
	fi

	if [ -f  "$SERVER_XML"_configureKeystoreBackupFile  ] ; then
	  ${RM} -f "$SERVER_XML"_configureKeystoreBackupFile
	fi
}

trap cleanup EXIT

function createNewKeystore(){

	_echo "Default generation of Key store will be Self Signed Certificate. For CA signed certificate follow Procedure mentioned in Troubleshooting Guide document."
	
	#Create backup of SSL directory and prepare directory to generate and store new key	store files
	$SU - dcuser -c "${MKDIR} -p ${HOME_DIR}/ssl_backup_folder"	
	${CHOWN} -R dcuser:dc5000 ${HOME_DIR}/ssl_backup_folder
	$SU - dcuser -c "${CP} -rf ${RT_DIR}/tomcat/ssl/* ${HOME_DIR}/ssl_backup_folder"
	
	${RM} -rf ${RT_DIR}/tomcat/ssl/*
	_echo "removed ssl files successfully"
	#$MV ${RT_DIR}/tomcat/ssl/* /eniq/home/dcuser/SSLFolderBackup
	#$MKDIR ${RT_DIR}/tomcat/ssl/private
	
	$SU - dcuser -c "${MKDIR} -p ${RT_DIR}/tomcat/ssl/private"
	${CHOWN} -R dcuser:dc5000 ${RT_DIR}/tomcat/ssl/private
	
	
	_echo "created private folder successfully"
	$CHMOD og-rwx ${RT_DIR}/tomcat/ssl/private
	
	#Now ssl folder is prepared with one directory private under this
	KEYSTOREPASSWORD=$1
	
	
	$KEYTOOL -genkeypair -keystore $JKEYSTORE -storepass ${KEYSTOREPASSWORD} -alias eniq -keypass ${KEYSTOREPASSWORD} -keysize 2048 -keyalg RSA -sigalg SHA256withRSA -dname "CN=$FULLNAME" -validity 825
	_echo "keystore.jks file created successfully."
	
	#Execute below command only if keystore.jks file generation got successfull.
	if [ $? -eq 0 ] ; then

		#Converting the existing JKS keystore to PKCS12 Keystore			
		_echo "Converting the existing JKS keystore to PKCS12 Keystore"
		$KEYTOOL -importkeystore -srckeystore $JKEYSTORE -destkeystore $P12KEYSTORE -srcstoretype JKS -deststoretype PKCS12 -srcstorepass ${KEYSTOREPASSWORD} -deststorepass ${KEYSTOREPASSWORD} -srcalias eniq -destalias eniq -srckeypass ${KEYSTOREPASSWORD} -destkeypass ${KEYSTOREPASSWORD}

		#Exporting Self_signed Certificate
		 _echo "Exporting Self_signed Certificate"
		$KEYTOOL -exportcert -keystore $JKEYSTORE -storepass ${KEYSTOREPASSWORD} -alias eniq -keypass ${KEYSTOREPASSWORD} -file $CERTFILE
		$CHMOD 0400 $CERTFILE

		#Generating Certificate Signing Request
		 _echo "Generating Certificate Signing Request"
		$KEYTOOL -certreq -keystore $JKEYSTORE -storepass ${KEYSTOREPASSWORD} -alias eniq -keypass ${KEYSTOREPASSWORD} -file $CSRFILE

		#Generating Private key
		 _echo "Generating Private key"
		$OPENSSL pkcs12 -in $P12KEYSTORE -out $PRIVATEKEY -passin pass:${KEYSTOREPASSWORD} -passout pass:${KEYSTOREPASSWORD}
		$CHMOD 0400 $PRIVATEKEY
		
		#restarting webserver to reflect changes
		$SU - dcuser -c "/eniq/sw/bin/webserver restart"
		if [ $? -ne 0 ] ; then
			_echo "Error in restarting the webserver service"
			exit 8
		else
			_echo "Webserver restarted successfully"
		fi
		
	else
		_echo "Failed to generate keystore.jks file!"
		exit 9
		
	fi
}

while getopts  "erpvfl:" flag ; do
	case $flag in
	v)
		VERBOSE=1
		;;
	f)
		FORCE=1
		;;
	l)  getLogFileName="$OPTARG"
		;;
	p)  Existing_keystore_pass=`inigetpassword KEYSTOREPASS -v keyStorePassValue -f $NIQ_INI`
		modifyPassInFilesAsDcuser $Existing_keystore_pass
		exit 0
		;;
	e)  validateNewPassword "Keystore"
		modifyPassInFilesAsDcuser $Keystore_password
		exit 0
		;;
	r)	if [ ${LOGNAME} != "root"  ] ; then
			_echo "This script must be executed as root"
			exit 1
		fi
		
		if [ ! -d "${LOG_DIR}/SSL" ] ; then
		  $SU - dcuser -c "${MKDIR} ${LOG_DIR}/SSL"
		fi

		LOG_FILE=${LOG_DIR}/SSL/keystore_${TIMESTAMP}.log
		$TOUCH $LOG_FILE
		
		_echo "WARNING!!! Reset mode is enabled. This option require some manual intervention and should only be used as documented in ENIQ System Administrator Guide under Section Troubleshooting Ericsson Network IQ."
		read -p 'Do you still want to continue? [YES | NO]: ' tempinput
		tempinput=`$ECHO $tempinput | tr '[:upper:]' '[:lower:]'`
		if [[ ${tempinput} == "yes" ]]; then
			validateNewPassword "New Keystore"	
			createNewKeystore $Keystore_password
			modifyPassInFiles $Keystore_password
		elif [[ ${tempinput} == "no" ]]; then
			exit 1
		else
			$ECHO "Cannot identify the input please try the options available [YES | NO]."
		fi	
		exit 0
		;;
	\?) exit 1
		;;
	esac
done

if [ ${LOGNAME} != "root"  ] ; then
  _echo "This script must be executed as root"
  exit 1
fi

if [ ! -d "${LOG_DIR}/SSL" ] ; then
  $SU - dcuser -c "${MKDIR} ${LOG_DIR}/SSL"
fi

LOG_FILE=${LOG_DIR}/SSL/keystore_${TIMESTAMP}.log
$TOUCH $LOG_FILE

function _debug(){
	if [ $VERBOSE = 1 ] ; then
		_echo ${*}
	fi
}
IP_ADDRESS=""
Keystore_password=""
Default_passValue=`inigetpassword KEYSTOREVALUE -v DefaultValue -f $DEFAULT_PLATFORM_INI`

while :; do
Keystore_password_old=`inigetpassword KEYSTOREPASS -v keyStorePassValue -f $NIQ_INI`
Default_passValue1=$(echo "$Default_passValue" | openssl enc -d -base64)

if [ "${Keystore_password_old}" == "${Default_passValue1}" ] ; then 
	break
else
	unset Keystore_password_old
	unset Keystore_password_old_niq
	_echo "Enter Old Keystore Password:"
    read -s Keystore_password_old
	Keystore_password_old_niq=`inigetpassword KEYSTOREPASS -v keyStorePassValue -f $NIQ_INI`
	
	if [ "$Keystore_password_old_niq" != "$Keystore_password_old" ] ; then
		_echo "Old keystore password is wrong."
		continue
	fi
	break
fi
done

validateNewPassword "New Keystore"

# Configuring the SSL directory.
_echo "Stopping Tomcat ..." 
if [ -x /eniq/sw/bin/webserver ] ; then
 $SU - dcuser -c "/eniq/sw/bin/webserver stop"
 _echo "Tomcat is stopped successfully"
else
_echo "Tomcat is not stopped"
fi
_echo "Configuring Tomcat for SSL ..."

if [ -d ${RT_DIR}/tomcat/ssl/ssl_backup/ ] ; then 
$SU - dcuser -c "${RM} -rf ${RT_DIR}/tomcat/ssl/ssl_backup"
  if [ $? -ne 0 ] ; then
  _echo "Error in removing ssl_backup directory "
  exit 3
  fi
fi
	
$SU - dcuser -c "${MKDIR} -p ${RT_DIR}/tomcat/ssl/ssl_backup"
if [ $? -ne 0 ] ; then
  _echo "Error in creating folder under ssl folder.Exiting.."
  exit 4
else
  _echo "ssl_backup directory created.."
fi	
	
shopt -s extglob; 
${CP} -rf ${RT_DIR}/tomcat/ssl/!(ssl_backup)  ${RT_DIR}/tomcat/ssl/ssl_backup
${CHOWN} -R dcuser:dc5000 ${RT_DIR}/tomcat/ssl/ssl_backup
if [ $? -eq 0 ] ; then
  _echo "All files under ssl folder is copied to ssl_backup"
else
  _echo "Error in copying files under ssl folder.Exiting.."
  exit 5
fi	
	
isKeyStoreUpdated="true"
	
shopt -s extglob
${RM} -rf /eniq/sw/runtime/tomcat/ssl/!(*.jks|ssl|ssl_backup)
if [ $? -eq 0 ] ; then
  _echo "All files under ssl folder is removed except keystore.jks"
else
  _echo "Error in removing files under ssl folder.Exiting.."
  exit 3
fi	
	
if [ ! -d ${RT_DIR}/tomcat/ssl/private ]; then
  $SU - dcuser -c "${MKDIR} -p ${RT_DIR}/tomcat/ssl/private"
fi
$SU - dcuser -c "${CHMOD} og-rwx ${RT_DIR}/tomcat/ssl/private"
	
if [ ! "${HOSTOUTPUT}" ]; then
	_echo "FULL name was not found in DNS lookup,using IP address " 
	ip_address
	FULLNAME=$IP_ADDRESS
fi

if [ -f  $JKEYSTORE  ] ; then
	$SU - dcuser -c "$KEYTOOL -storepasswd -new '$Keystore_password' -keystore $JKEYSTORE -storepass '$Keystore_password_old'"
	$SU - dcuser -c "$KEYTOOL -keypasswd -keypass '$Keystore_password_old' -new '$Keystore_password' -keystore $JKEYSTORE -alias eniq -storepass '$Keystore_password'"
	if [ $? -ne 0 ] ; then
	  _echo "Error in changing keystore password for JKS keystore.Exiting..."
	  restore_configuration
	  exit 6
	else
	  _echo "keystore password for keystore.jks updated successfully"
	  _echo "Generating PKCS12 Keystore" 
	  $SU - dcuser -c "$KEYTOOL -importkeystore -srckeystore $JKEYSTORE -destkeystore $P12KEYSTORE -srcstoretype JKS -deststoretype PKCS12 -srcstorepass '${Keystore_password}' -deststorepass '${Keystore_password}' -srcalias eniq -destalias eniq -srckeypass '${Keystore_password}' -destkeypass '${Keystore_password}'"
	  _echo "Exporting Self_signed Certificate" 
	  $SU - dcuser -c "$KEYTOOL -exportcert -keystore $JKEYSTORE -storepass '${Keystore_password}' -alias eniq -keypass '${Keystore_password}' -file $CERTFILE"
	  $SU - dcuser -c "${CHMOD} 0400 $CERTFILE"
	  _echo "Generating Certificate Signing Request" 
	  $SU - dcuser -c "$KEYTOOL -certreq -keystore $JKEYSTORE -storepass '${Keystore_password}' -alias eniq -keypass '${Keystore_password}' -file $CSRFILE"		
	  _echo "Generating Private key" 
	  $SU - dcuser -c "$OPENSSL pkcs12 -in $P12KEYSTORE -out $PRIVATEKEY -passin pass:'${Keystore_password}' -passout pass:'${Keystore_password}'"
	  $SU - dcuser -c "${CHMOD} 0400 $PRIVATEKEY"
	fi
	
	_echo "Tomcat is configured with the new Keystore password."
else
	_echo "$JKEYSTORE file not found. Tomcat is not configured with the new Keystore password."
fi
		
# Configuring truststore.ts
if [ -f  $TRUSTSTORE  ] ; then
	if [ -f  "$TRUSTSTORE"_configureKeystoreBackupFile  ] ; then
	  ${RM} -f "$TRUSTSTORE"_configureKeystoreBackupFile
	fi
	$SU - dcuser -c "${CP} -f $TRUSTSTORE ${TRUSTSTORE}_configureKeystoreBackupFile"
	
	$SU - dcuser -c "$KEYTOOL -storepasswd -new '$Keystore_password' -keystore $TRUSTSTORE -storepass '$Keystore_password_old'"
	if [ $? -ne 0 ] ; then
	  _echo "Error in changing keystore password for truststore.ts"
	  restore_configuration
	  exit 6
    else
	  _echo "keystore password for truststore.ts updated successfully"
    fi
else
	_echo "$TRUSTSTORE not found."
fi

# Configuring password in niq.ini and server.xml
modifyPassInFiles $Keystore_password

$SU - dcuser -c "/eniq/sw/bin/engine restart"
if [ $? -ne 0 ] ; then
	_echo "Error in restarting the engine service"
	exit 8
else
	_echo "Engine restarted successfully"
fi
