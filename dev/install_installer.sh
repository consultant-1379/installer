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
# Name    : install_installer.sh
# Date    : 15/07/2020(dummy date) Last modified 10/05/2023
# Purpose : Ericsson Network IQ Installer installation script
# Usage   : install_installer.sh [-v]
# ********************************************************************

############## THE SCRIPT BEGINS HERE ##############
. /eniq/home/dcuser/.profile

VERBOSE=0

if [ "$1" = "-v" ] ; then
  VERBOSE=1
fi

if [ -z "${CONF_DIR}" ] ; then
  echo "ERROR: CONF_DIR is not set"
  exit 1
fi

. ${CONF_DIR}/niq.rc
. ${BIN_DIR}/common_variables.lib

TIMESTAMP=`$DATE +%d.%m.%y_%H:%M:%S`

LOGFILE=${LOG_DIR}/platform_installer/installer_${TIMESTAMP}.log

if [ -z "${INSTALLER_DIR}" ] ; then
  ${ECHO} "ERROR: INSTALLER_DIR is not defined" | ${TEE} -a ${LOGFILE}
  exit 2
fi

if [ ${VERBOSE} = 1 ] ; then
  ${ECHO} "Installing platform_installer" | ${TEE} -a ${LOGFILE}
fi
if [ -f ${INSTALLER_DIR}/platform_installer ] ; then
  ${RM} -f ${INSTALLER_DIR}/platform_installer | ${TEE} -a ${LOGFILE}
fi
${CP} installer/platform_installer ${INSTALLER_DIR}/platform_installer | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${INSTALLER_DIR}/platform_installer | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/tasks_platform_installer.xml ] ; then
  ${RM} -f ${INSTALLER_DIR}/tasks_platform_installer.xml | ${TEE} -a ${LOGFILE}
fi
${CP} installer/tasks_platform_installer.xml ${INSTALLER_DIR}/tasks_platform_installer.xml | ${TEE} -a ${LOGFILE}
${CHMOD} 440 ${INSTALLER_DIR}/tasks_platform_installer.xml | ${TEE} -a ${LOGFILE}

if [ ${VERBOSE} = 1 ] ; then
  ${ECHO} "Installing tp_installer" | ${TEE} -a ${LOGFILE}
fi
if [ -f ${INSTALLER_DIR}/tp_installer ] ; then
  ${RM} -f ${INSTALLER_DIR}/tp_installer | ${TEE} -a ${LOGFILE}
fi
${CP} installer/tp_installer ${INSTALLER_DIR}/tp_installer | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${INSTALLER_DIR}/tp_installer | ${TEE} -a ${LOGFILE}

if [ ${VERBOSE} = 1 ] ; then
  ${ECHO} "Installing set_correct_option_value.bsh" | ${TEE} -a ${LOGFILE}
fi
if [ -f ${INSTALLER_DIR}/set_correct_option_value.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/set_correct_option_value.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/set_correct_option_value.bsh ${INSTALLER_DIR}/set_correct_option_value.bsh | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${INSTALLER_DIR}/set_correct_option_value.bsh | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/tpec ] ; then
  ${RM} -f ${INSTALLER_DIR}/tpec | ${TEE} -a ${LOGFILE}
fi


if [ -f ${INSTALLER_DIR}/tasks_tp_installer.xml ] ; then
  ${RM} -f ${INSTALLER_DIR}/tasks_tp_installer.xml | ${TEE} -a ${LOGFILE}
fi
${CP} installer/tasks_tp_installer.xml ${INSTALLER_DIR}/tasks_tp_installer.xml | ${TEE} -a ${LOGFILE}
${CHMOD} 440 ${INSTALLER_DIR}/tasks_tp_installer.xml | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/tasks_install_utils.xml ] ; then
  ${RM} -f ${INSTALLER_DIR}/tasks_install_utils.xml | ${TEE} -a ${LOGFILE}
fi
${CP} installer/tasks_install_utils.xml ${INSTALLER_DIR}/tasks_install_utils.xml | ${TEE} -a ${LOGFILE}
${CHMOD} 440 ${INSTALLER_DIR}/tasks_install_utils.xml | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/tasks_extract_reportpacks.xml ] ; then
  ${RM} -f ${INSTALLER_DIR}/tasks_extract_reportpacks.xml | ${TEE} -a ${LOGFILE}
fi
${CP} installer/tasks_extract_reportpacks.xml ${INSTALLER_DIR}/tasks_extract_reportpacks.xml | ${TEE} -a ${LOGFILE}
${CHMOD} 440 ${INSTALLER_DIR}/tasks_extract_reportpacks.xml | ${TEE} -a ${LOGFILE}

if [ ! -d ${INSTALLER_DIR}/lib ] ; then
  ${MKDIR} ${INSTALLER_DIR}/lib | ${TEE} -a ${LOGFILE}
fi
if [ -f ${INSTALLER_DIR}/lib/installer.jar ] ; then
  ${RM} -f ${INSTALLER_DIR}/lib/installer.jar | ${TEE} -a ${LOGFILE}
fi
${CP} lib/installer.jar ${INSTALLER_DIR}/lib/ | ${TEE} -a ${LOGFILE}
${CHMOD} 440 ${INSTALLER_DIR}/lib/installer.jar | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/extract_report_packages.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/extract_report_packages.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/extract_report_packages.bsh ${INSTALLER_DIR}/extract_report_packages.bsh | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${INSTALLER_DIR}/extract_report_packages.bsh | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/load_tp_node_version.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/load_tp_node_version.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/load_tp_node_version.bsh ${INSTALLER_DIR}/load_tp_node_version.bsh | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${INSTALLER_DIR}/load_tp_node_version.bsh | ${TEE} -a ${LOGFILE}

if [ -f ${CONF_DIR}/TP_R_NodeVersion.txt ] ; then
  ${RM} -f ${CONF_DIR}/TP_R_NodeVersion.txt | ${TEE} -a ${LOGFILE}
fi
${CP} installer/TP_R_NodeVersion.txt ${CONF_DIR}/TP_R_NodeVersion.txt| ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${CONF_DIR}/TP_R_NodeVersion.txt | ${TEE} -a ${LOGFILE}

if [ -f ${CONF_DIR}/default_platform.ini ] ; then
  ${RM} -f ${CONF_DIR}/default_platform.ini | ${TEE} -a ${LOGFILE}
fi
${CP} installer/default_platform.ini ${CONF_DIR}/default_platform.ini| ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${CONF_DIR}/default_platform.ini | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/activate_interface ] ; then
  ${RM} -f ${INSTALLER_DIR}/activate_interface | ${TEE} -a ${LOGFILE}
fi
${CP} installer/activate_interface ${INSTALLER_DIR}/activate_interface | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${INSTALLER_DIR}/activate_interface | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/deactivate_interface ] ; then
  ${RM} -f ${INSTALLER_DIR}/deactivate_interface | ${TEE} -a ${LOGFILE}
fi
${CP} installer/deactivate_interface ${INSTALLER_DIR}/deactivate_interface | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${INSTALLER_DIR}/deactivate_interface | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/reactivate_interfaces ] ; then
  ${RM} -f ${INSTALLER_DIR}/reactivate_interfaces | ${TEE} -a ${LOGFILE}
fi
${CP} installer/reactivate_interfaces ${INSTALLER_DIR}/reactivate_interfaces | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${INSTALLER_DIR}/reactivate_interfaces | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/installed_techpacks ] ; then
  ${RM} -f ${INSTALLER_DIR}/installed_techpacks | ${TEE} -a ${LOGFILE}
fi
${CP} installer/installed_techpacks ${INSTALLER_DIR}/installed_techpacks | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${INSTALLER_DIR}/installed_techpacks | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/get_active_interfaces ] ; then
  ${RM} -f ${INSTALLER_DIR}/get_active_interfaces| ${TEE} -a ${LOGFILE}
fi
${CP} installer/get_active_interfaces ${INSTALLER_DIR}/get_active_interfaces | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${INSTALLER_DIR}/get_active_interfaces | ${TEE} -a ${LOGFILE}

if [ ${VERBOSE} = 1 ] ; then
  ${ECHO} "Installing snapshot_functions.bsh" | ${TEE} -a ${LOGFILE}
fi
if [ -f ${INSTALLER_DIR}/snapshot_functions.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/snapshot_functions.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/snapshot_functions.bsh ${INSTALLER_DIR}/snapshot_functions.bsh | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${INSTALLER_DIR}/snapshot_functions.bsh | ${TEE} -a ${LOGFILE}

if [ ${VERBOSE} = 1 ] ; then
  ${ECHO} "Installing change_db_users_perm.bsh" | ${TEE} -a ${LOGFILE}
fi
if [ -f ${INSTALLER_DIR}/change_db_users_perm.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/change_db_users_perm.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/change_db_users_perm.bsh ${INSTALLER_DIR}/change_db_users_perm.bsh | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${INSTALLER_DIR}/change_db_users_perm.bsh | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/run_dir_checker.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/run_dir_checker.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/run_dir_checker.bsh ${INSTALLER_DIR}/run_dir_checker.bsh | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${INSTALLER_DIR}/run_dir_checker.bsh | ${TEE} -a ${LOGFILE}

if [ ${VERBOSE} = 1 ] ; then
  ${ECHO} "Installing restore_dwhdb_database" | ${TEE} -a ${LOGFILE}
fi
if [ -f ${INSTALLER_DIR}/restore_dwhdb_database ] ; then
  ${RM} -f ${INSTALLER_DIR}/restore_dwhdb_database | ${TEE} -a ${LOGFILE}
fi
${CP} installer/restore_dwhdb_database ${INSTALLER_DIR}/restore_dwhdb_database | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${INSTALLER_DIR}/restore_dwhdb_database | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/tasks_restore_dwhdb_utils.xml ] ; then
  ${RM} -f ${INSTALLER_DIR}/tasks_restore_dwhdb_utils.xml | ${TEE} -a ${LOGFILE}
fi
${CP} installer/tasks_restore_dwhdb_utils.xml ${INSTALLER_DIR}/tasks_restore_dwhdb_utils.xml | ${TEE} -a ${LOGFILE}
${CHMOD} 440 ${INSTALLER_DIR}/tasks_restore_dwhdb_utils.xml | ${TEE} -a ${LOGFILE}

if [ ${VERBOSE} = 1 ] ; then
  ${ECHO} "Installing roll_over_partition" | ${TEE} -a ${LOGFILE}
fi
if [ -f ${INSTALLER_DIR}/partition_roll_over ] ; then
  ${RM} -f ${INSTALLER_DIR}/partition_roll_over | ${TEE} -a ${LOGFILE}
fi
${CP} installer/partition_roll_over ${INSTALLER_DIR}/partition_roll_over | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${INSTALLER_DIR}/partition_roll_over | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/tasks_partition_roll_over_to_old_techpack.xml ] ; then
  ${RM} -f ${INSTALLER_DIR}/tasks_partition_roll_over_to_old_techpack.xml | ${TEE} -a ${LOGFILE}
fi
${CP} installer/tasks_partition_roll_over_to_old_techpack.xml ${INSTALLER_DIR}/tasks_partition_roll_over_to_old_techpack.xml | ${TEE} -a ${LOGFILE}
${CHMOD} 440 ${INSTALLER_DIR}/tasks_partition_roll_over_to_old_techpack.xml | ${TEE} -a ${LOGFILE}

if [ ${VERBOSE} = 1 ] ; then
  ${ECHO} "Installing add_agg_flag" | ${TEE} -a ${LOGFILE}
fi
if [ -f ${INSTALLER_DIR}/add_agg_flag ] ; then
  ${RM} -f ${INSTALLER_DIR}/add_agg_flag | ${TEE} -a ${LOGFILE}
fi
${CP} installer/add_agg_flag ${INSTALLER_DIR}/add_agg_flag | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${INSTALLER_DIR}/add_agg_flag | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/tasks_add_agg_flag_to_old_techpack.xml ] ; then
  ${RM} -f ${INSTALLER_DIR}/tasks_add_agg_flag_to_old_techpack.xml | ${TEE} -a ${LOGFILE}
fi
${CP} installer/tasks_add_agg_flag_to_old_techpack.xml ${INSTALLER_DIR}/tasks_add_agg_flag_to_old_techpack.xml | ${TEE} -a ${LOGFILE}
${CHMOD} 440 ${INSTALLER_DIR}/tasks_add_agg_flag_to_old_techpack.xml | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/tasks_execute_partiton_upgrade_sql.xml ] ; then
  ${RM} -f ${INSTALLER_DIR}/tasks_execute_partiton_upgrade_sql.xml | ${TEE} -a ${LOGFILE}
fi
${CP} installer/tasks_execute_partiton_upgrade_sql.xml ${INSTALLER_DIR}/tasks_execute_partiton_upgrade_sql.xml | ${TEE} -a ${LOGFILE}
${CHMOD} 440 ${INSTALLER_DIR}/tasks_execute_partiton_upgrade_sql.xml | ${TEE} -a ${LOGFILE}

if [ ${VERBOSE} = 1 ] ; then
  ${ECHO} "Installing datetime_minute_upgrade" | ${TEE} -a ${LOGFILE}
fi
if [ -f ${INSTALLER_DIR}/datetime_minute_upgrade.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/datetime_minute_upgrade.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/datetime_minute_upgrade.bsh ${INSTALLER_DIR}/datetime_minute_upgrade.bsh | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${INSTALLER_DIR}/datetime_minute_upgrade.bsh | ${TEE} -a ${LOGFILE}

if [ ${VERBOSE} = 1 ] ; then
  ${ECHO} "Installing change_dc_db_url" | ${TEE} -a ${LOGFILE}
fi
if [ -f ${INSTALLER_DIR}/change_dc_db_url.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/change_dc_db_url.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/change_dc_db_url.bsh ${INSTALLER_DIR}/change_dc_db_url.bsh | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${INSTALLER_DIR}/change_dc_db_url.bsh | ${TEE} -a ${LOGFILE}

if [ ${VERBOSE} = 1 ] ; then
  ${ECHO} "Installing run_if_file_exists.bsh" | ${TEE} -a ${LOGFILE}
fi
if [ -f ${INSTALLER_DIR}/run_if_file_exists.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/run_if_file_exists.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/run_if_file_exists.bsh ${INSTALLER_DIR}/run_if_file_exists.bsh | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${INSTALLER_DIR}/run_if_file_exists.bsh | ${TEE} -a ${LOGFILE}

if [ -f ${BIN_DIR}/set_maxper_run_value.bsh ] ; then
  ${RM} -f ${BIN_DIR}/set_maxper_run_value.bsh | ${TEE} -a ${LOGFILE}
fi
#Fix for HU53026
#${CP} installer/set_maxper_run_value.bsh ${BIN_DIR}/set_maxper_run_value.bsh | ${TEE} -a ${LOGFILE}
#${CHMOD} 550 ${BIN_DIR}/set_maxper_run_value.bsh | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/change_catalog_cache.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/change_catalog_cache.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/change_catalog_cache.bsh ${INSTALLER_DIR}/change_catalog_cache.bsh | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${INSTALLER_DIR}/change_catalog_cache.bsh | ${TEE} -a ${LOGFILE}

if [ -f ${BIN_DIR}/MaxFilePerRunReduced.jar ] ; then
  ${RM} -f ${BIN_DIR}/MaxFilePerRunReduced.jar | ${TEE} -a ${LOGFILE}
fi

#XARJSIN - Adding scripts for FFU EQEV-41981

if [ -f ${INSTALLER_DIR}/FFU_trigger_upgrade.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/FFU_trigger_upgrade.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/FFU_trigger_upgrade.bsh ${INSTALLER_DIR}/FFU_trigger_upgrade.bsh | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${INSTALLER_DIR}/FFU_trigger_upgrade.bsh | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/cleanup_FFU.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/cleanup_FFU.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/cleanup_FFU.bsh ${INSTALLER_DIR}/cleanup_FFU.bsh | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${INSTALLER_DIR}/cleanup_FFU.bsh | ${TEE} -a ${LOGFILE}

#END of FFU changes

#Fix for HU53026	
#${CP} installer/MaxFilePerRunReduced.jar ${BIN_DIR}/MaxFilePerRunReduced.jar | ${TEE} -a ${LOGFILE}
#${CHMOD} 550 ${BIN_DIR}/MaxFilePerRunReduced.jar | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/post_rollback.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/post_rollback.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/post_rollback.bsh ${INSTALLER_DIR}/post_rollback.bsh | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${INSTALLER_DIR}/post_rollback.bsh | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/post_integration_script.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/post_integration_script.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/post_integration_script.bsh ${INSTALLER_DIR}/post_integration_script.bsh | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/post_integration_script.bsh | ${TEE} -a ${LOGFILE}

# ---------------------------------------------------------------------
# Adding update_bulkcm_interval.bsh as part of cENM MR: EQEV-55953
# for EQEV-108268
# ---------------------------------------------------------------------
if [ -f ${INSTALLER_DIR}/update_bulkcm_interval.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/update_bulkcm_interval.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/update_bulkcm_interval.bsh ${INSTALLER_DIR}/update_bulkcm_interval.bsh | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/update_bulkcm_interval.bsh | ${TEE} -a ${LOGFILE}

if [ -f ${ADMIN_BIN}/twamppoller.bsh ] ; then
  ${RM} -f ${ADMIN_BIN}/twamppoller.bsh | ${TEE} -a ${LOGFILE}
fi

if [ -f ${BIN_DIR}/remove_hidden_files.bsh ] ; then
  ${RM} -f ${BIN_DIR}/remove_hidden_files.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/remove_hidden_files.bsh ${BIN_DIR}/remove_hidden_files.bsh | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${BIN_DIR}/remove_hidden_files.bsh | ${TEE} -a ${LOGFILE};

#For EQEV-93129
if [ -f ${BIN_DIR}/rebuildIndex.bsh ] ; then
  ${RM} -f ${BIN_DIR}/rebuildIndex.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/rebuildIndex.bsh ${BIN_DIR}/rebuildIndex.bsh | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${BIN_DIR}/rebuildIndex.bsh | ${TEE} -a ${LOGFILE};

# For EQEV-34540
if [ -f ${BIN_DIR}/NetAnFileHandler.sh ] ; then
  ${RM} -f ${BIN_DIR}/NetAnFileHandler.sh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/NetAnFileHandler.sh ${BIN_DIR}/NetAnFileHandler.sh | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${BIN_DIR}/NetAnFileHandler.sh | ${TEE} -a ${LOGFILE};

if [ ${VERBOSE} = 1 ] ; then
  ${ECHO} "Installing SOEM/IPTNMS setup files" | ${TEE} -a ${LOGFILE}
fi

# Adding IPTNMS/SOEM Import File
if [ -f ${BIN_DIR}/soem_iptnms_import.bsh ] ; then
  ${RM} -f ${BIN_DIR}/soem_iptnms_import.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/soem_iptnms_import.bsh ${BIN_DIR}/soem_iptnms_import.bsh | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${BIN_DIR}/soem_iptnms_import.bsh | ${TEE} -a ${LOGFILE}

# Adding IPTNMS/SOEM Setup File
if [ -f ${BIN_DIR}/soem_iptnms_setup.bsh ] ; then
  ${RM} -f ${BIN_DIR}/soem_iptnms_setup.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/soem_iptnms_setup.bsh ${BIN_DIR}/soem_iptnms_setup.bsh | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${BIN_DIR}/soem_iptnms_setup.bsh | ${TEE} -a ${LOGFILE}

# Adding IPTNMS/SOEM Config File
if [ ! -f ${CONF_DIR}/soem_iptnms_config.ini ] ; then
  ${CP} installer/soem_iptnms_config.ini ${CONF_DIR}/soem_iptnms_config.ini | ${TEE} -a ${LOGFILE}
  ${CHMOD} 740 ${CONF_DIR}/soem_iptnms_config.ini | ${TEE} -a ${LOGFILE}
#start - Fix for TR HT82252
elif [ -f ${CONF_DIR}/soem_iptnms_config.ini ] ; then
        ${GREP} -c "\*_Configuration_Data_\*" ${CONF_DIR}/soem_iptnms_config.ini > /dev/null 2>&1
        if [ $? = 0 ]; then
                ${CP} installer/soem_iptnms_config.ini ${CONF_DIR}/soem_iptnms_config.ini | ${TEE} -a ${LOGFILE}
                ${CHMOD} 740 ${CONF_DIR}/soem_iptnms_config.ini | ${TEE} -a ${LOGFILE}
        fi
fi

if [ -f ${INSTALLER_DIR}/data_delete.sh ] ; then
  ${RM} -f ${INSTALLER_DIR}/data_delete.sh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/data_delete.sh ${INSTALLER_DIR}/data_delete.sh | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/data_delete.sh | ${TEE} -a ${LOGFILE}
#end - Fix for TR HT82252

# Adding remove_mediator File
if [ -f ${ADMIN_BIN}/remove_mediator.bsh ] ; then
  ${RM} -f ${ADMIN_BIN}/remove_mediator.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/remove_mediator.bsh ${ADMIN_BIN}/remove_mediator.bsh | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${ADMIN_BIN}/remove_mediator.bsh | ${TEE} -a ${LOGFILE}

if [ -f ${BIN_DIR}/count_repbd_dwhdb_conn.bsh ] ; then
  ${RM} -f ${BIN_DIR}/count_repbd_dwhdb_conn.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/count_repbd_dwhdb_conn.bsh ${BIN_DIR}/count_repbd_dwhdb_conn.bsh | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${BIN_DIR}/count_repbd_dwhdb_conn.bsh | ${TEE} -a ${LOGFILE}

if [ -f ${BIN_DIR}/count_dwhdb_conn.sql ] ; then
  ${RM} -f ${BIN_DIR}/count_dwhdb_conn.sql | ${TEE} -a ${LOGFILE}
fi
${CP} installer/count_dwhdb_conn.sql ${BIN_DIR}/count_dwhdb_conn.sql | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${BIN_DIR}/count_dwhdb_conn.sql | ${TEE} -a ${LOGFILE}

if [ -f ${BIN_DIR}/count_repdb_conn.sql ] ; then
  ${RM} -f ${BIN_DIR}/count_repdb_conn.sql | ${TEE} -a ${LOGFILE}
fi
${CP} installer/count_repdb_conn.sql ${BIN_DIR}/count_repdb_conn.sql | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${BIN_DIR}/count_repdb_conn.sql | ${TEE} -a ${LOGFILE}

if [ -f ${BIN_DIR}/create_rep_dwh_temp.sql ] ; then
  ${RM} -f ${BIN_DIR}/create_rep_dwh_temp.sql | ${TEE} -a ${LOGFILE}
fi
${CP} installer/create_rep_dwh_temp.sql ${BIN_DIR}/create_rep_dwh_temp.sql | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${BIN_DIR}/create_rep_dwh_temp.sql | ${TEE} -a ${LOGFILE}

if [ -f ${BIN_DIR}/Topo_Null_Check.bsh ] ; then
  ${RM} -f ${BIN_DIR}/Topo_Null_Check.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/Topo_Null_Check.bsh ${BIN_DIR}/Topo_Null_Check.bsh | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${BIN_DIR}/Topo_Null_Check.bsh | ${TEE} -a ${LOGFILE}

if [ -f ${ADMIN_BIN}/ServiceRestart.bsh ] ; then
  ${RM} -f ${ADMIN_BIN}/ServiceRestart.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/ServiceRestart.bsh ${INSTALLER_DIR}/ServiceRestart.bsh  | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/ServiceRestart.bsh  | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/sybase_log.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/sybase_log.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/sybase_log.bsh ${INSTALLER_DIR}/sybase_log.bsh  | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${INSTALLER_DIR}/sybase_log.bsh  | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/platform_log.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/platform_log.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/platform_log.bsh ${INSTALLER_DIR}/platform_log.bsh  | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${INSTALLER_DIR}/platform_log.bsh  | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/runtime_log.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/runtime_log.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/runtime_log.bsh ${INSTALLER_DIR}/runtime_log.bsh  | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/runtime_log.bsh  | ${TEE} -a ${LOGFILE}

if [ -f ${BIN_DIR}/monitor_heap.bsh ] ; then
  ${RM} -f ${BIN_DIR}/monitor_heap.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/monitor_heap.bsh ${BIN_DIR}/monitor_heap.bsh  | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${BIN_DIR}/monitor_heap.bsh  | ${TEE} -a ${LOGFILE}

if [ -f ${BIN_DIR}/monitor_cache_usage.bsh ] ; then
  ${RM} -f ${BIN_DIR}/monitor_cache_usage.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/monitor_cache_usage.bsh ${BIN_DIR}/monitor_cache_usage.bsh  | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${BIN_DIR}/monitor_cache_usage.bsh | ${TEE} -a ${LOGFILE}


if [ -f ${BIN_DIR}/create_engine_heap.sql ] ; then
 ${RM} -f ${BIN_DIR}/create_engine_heap.sql | ${TEE} -a ${LOGFILE}
fi
${CP} installer/create_engine_heap.sql ${BIN_DIR}/create_engine_heap.sql | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${BIN_DIR}/create_engine_heap.sql | ${TEE} -a ${LOGFILE}


if [ -f ${BIN_DIR}/create_monitor_cache.sql ] ; then 
 ${RM} -f ${BIN_DIR}/create_monitor_cache.sql | ${TEE} -a ${LOGFILE}
fi
${CP} installer/create_monitor_cache.sql ${BIN_DIR}/create_monitor_cache.sql | ${TEE} -a ${LOGFILE}
${CHMOD} 540 ${BIN_DIR}/create_monitor_cache.sql | ${TEE} -a ${LOGFILE}

if [ -f ${BIN_DIR}/MovedFailedLoaderFile.bsh ] ; then 
 ${RM} -f ${BIN_DIR}/MovedFailedLoaderFile.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/MovedFailedLoaderFile.bsh ${BIN_DIR}/MovedFailedLoaderFile.bsh | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${BIN_DIR}/MovedFailedLoaderFile.bsh | ${TEE} -a ${LOGFILE}

if [ -f ${BIN_DIR}/loader_delimiter.bsh ] ; then
 ${RM} -f ${BIN_DIR}/loader_delimiter.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/loader_delimiter.bsh ${BIN_DIR}/loader_delimiter.bsh | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${BIN_DIR}/loader_delimiter.bsh | ${TEE} -a ${LOGFILE}

if [ -f ${BIN_DIR}/collect_certificates.bsh ] ; then
 ${RM} -f ${BIN_DIR}/collect_certificates.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/collect_certificates.bsh ${BIN_DIR}/collect_certificates.bsh | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${BIN_DIR}/collect_certificates.bsh | ${TEE} -a ${LOGFILE}


if [ -f ${INSTALLER_DIR}/generate_erbs_combined_view.sql ] ; then
  ${RM} -f ${INSTALLER_DIR}/generate_erbs_combined_view.sql | ${TEE} -a ${LOGFILE}
fi
${CP} installer/generate_erbs_combined_view.sql ${INSTALLER_DIR}/generate_erbs_combined_view.sql  | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/generate_erbs_combined_view.sql  | ${TEE} -a ${LOGFILE}

# if [ -f ${INSTALLER_DIR}/erbscombinedview.bsh ] ; then
  # ${RM} -f ${INSTALLER_DIR}/erbscombinedview.bsh | ${TEE} -a ${LOGFILE}
# fi
# ${CP} installer/erbscombinedview.bsh ${INSTALLER_DIR}/erbscombinedview.bsh  | ${TEE} -a ${LOGFILE}
# ${CHMOD} 750 ${INSTALLER_DIR}/erbscombinedview.bsh  | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/generate_DIM_E_LTE_LLE_EUCELL_view.sql ] ; then
  ${RM} -f ${INSTALLER_DIR}/generate_DIM_E_LTE_LLE_EUCELL_view.sql | ${TEE} -a ${LOGFILE}
fi
${CP} installer/generate_DIM_E_LTE_LLE_EUCELL_view.sql ${INSTALLER_DIR}/generate_DIM_E_LTE_LLE_EUCELL_view.sql  | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/generate_DIM_E_LTE_LLE_EUCELL_view.sql  | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/generate_DIM_E_LTE_LLE_ERBS_view.sql ] ; then
  ${RM} -f ${INSTALLER_DIR}/generate_DIM_E_LTE_LLE_ERBS_view.sql | ${TEE} -a ${LOGFILE}
fi
${CP} installer/generate_DIM_E_LTE_LLE_ERBS_view.sql ${INSTALLER_DIR}/generate_DIM_E_LTE_LLE_ERBS_view.sql  | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/generate_DIM_E_LTE_LLE_ERBS_view.sql  | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/generate_rbs_combined_view.sql ] ; then
  ${RM} -f ${INSTALLER_DIR}/generate_rbs_combined_view.sql | ${TEE} -a ${LOGFILE}
fi
${CP} installer/generate_rbs_combined_view.sql ${INSTALLER_DIR}/generate_rbs_combined_view.sql  | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/generate_rbs_combined_view.sql  | ${TEE} -a ${LOGFILE}

# if [ -f ${INSTALLER_DIR}/WCDMACombinedViewConfigFile.csv ] ; then
  # ${RM} -f ${INSTALLER_DIR}/WCDMACombinedViewConfigFile.csv | ${TEE} -a ${LOGFILE}
# fi
# ${CP} installer/WCDMACombinedViewConfigFile.csv ${INSTALLER_DIR}/WCDMACombinedViewConfigFile.csv  | ${TEE} -a ${LOGFILE}
# ${CHMOD} 750 ${INSTALLER_DIR}/WCDMACombinedViewConfigFile.csv  | ${TEE} -a ${LOGFILE}

# if [ -f ${INSTALLER_DIR}/WCDMACombinedViewCreation.bsh ] ; then
  # ${RM} -f ${INSTALLER_DIR}/WCDMACombinedViewCreation.bsh | ${TEE} -a ${LOGFILE}
# fi
# ${CP} installer/WCDMACombinedViewCreation.bsh ${INSTALLER_DIR}/WCDMACombinedViewCreation.bsh  | ${TEE} -a ${LOGFILE}
# ${CHMOD} 750 ${INSTALLER_DIR}/WCDMACombinedViewCreation.bsh  | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/dcpublic_wcdma.sql ] ; then
  ${RM} -f ${INSTALLER_DIR}/dcpublic_wcdma.sql | ${TEE} -a ${LOGFILE}
fi
${CP} installer/dcpublic_wcdma.sql ${INSTALLER_DIR}/dcpublic_wcdma.sql  | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/dcpublic_wcdma.sql  | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/dcpublic_erbs.sql ] ; then
  ${RM} -f ${INSTALLER_DIR}/dcpublic_erbs.sql | ${TEE} -a ${LOGFILE}
fi
${CP} installer/dcpublic_erbs.sql ${INSTALLER_DIR}/dcpublic_erbs.sql | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/dcpublic_erbs.sql  | ${TEE} -a ${LOGFILE}


if [ -f ${INSTALLER_DIR}/install_parsers.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/install_parsers.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/install_parsers.bsh ${INSTALLER_DIR}/install_parsers.bsh  | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/install_parsers.bsh  | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/disableOrEnableBusyHourSets.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/disableOrEnableBusyHourSets.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/disableOrEnableBusyHourSets.bsh ${INSTALLER_DIR}/disableOrEnableBusyHourSets.bsh  | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/disableOrEnableBusyHourSets.bsh  | ${TEE} -a ${LOGFILE}

#if [ -f ${INSTALLER_DIR}/deltaviewcreation.bsh ] ; then
#  ${RM} -f ${INSTALLER_DIR}/deltaviewcreation.bsh | ${TEE} -a ${LOGFILE}
#fi
#${CP} installer/deltaviewcreation.bsh ${INSTALLER_DIR}/deltaviewcreation.bsh  | ${TEE} -a ${LOGFILE}
#${CHMOD} 750 ${INSTALLER_DIR}/deltaviewcreation.bsh  | ${TEE} -a ${LOGFILE} 


if [ -f ${INSTALLER_DIR}/configure_newkeystore.sh ] ; then
  ${RM} -f ${INSTALLER_DIR}/configure_newkeystore.sh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/configure_newkeystore.sh ${INSTALLER_DIR}/configure_newkeystore.sh  | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/configure_newkeystore.sh  | ${TEE} -a ${LOGFILE} 

if [ -f ${ADMIN_BIN}/scheduler_check.bsh ] ; then
  ${RM} -f ${ADMIN_BIN}/scheduler_check.bsh | ${TEE} -a ${LOGFILE}
fi

if [ -f ${ADMIN_BIN}/FileSystemCheck.bsh ] ; then
  ${RM} -f ${ADMIN_BIN}/FileSystemCheck.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/FileSystemCheck.bsh ${ADMIN_BIN}/FileSystemCheck.bsh  | ${TEE} -a ${LOGFILE}
${CHMOD} 640 ${ADMIN_BIN}/FileSystemCheck.bsh  | ${TEE} -a ${LOGFILE}

###########################EQEV-68717##########################################
if [ -f ${ADMIN_BIN}/Security_Rollback_Procedure.bsh ] ; then
  ${RM} -f ${ADMIN_BIN}/Security_Rollback_Procedure.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/Security_Rollback_Procedure.bsh ${ADMIN_BIN}/Security_Rollback_Procedure.bsh  | ${TEE} -a ${LOGFILE}
${CHMOD} 640 ${ADMIN_BIN}/Security_Rollback_Procedure.bsh  | ${TEE} -a ${LOGFILE}

###############################################################################

# if [ -f ${INSTALLER_DIR}/delete_duplicate_corrective.bsh ] ; then
  # ${RM} -f ${INSTALLER_DIR}/delete_duplicate_corrective.bsh | ${TEE} -a ${LOGFILE}
# fi
# ${CP} installer/delete_duplicate_corrective.bsh ${INSTALLER_DIR}/delete_duplicate_corrective.bsh  | ${TEE} -a ${LOGFILE}
# ${CHMOD} 750 ${INSTALLER_DIR}/delete_duplicate_corrective.bsh  | ${TEE} -a ${LOGFILE}

# if [ -f ${INSTALLER_DIR}/delete_duplicate_preventive.bsh ] ; then
  # ${RM} -f ${INSTALLER_DIR}/delete_duplicate_preventive.bsh | ${TEE} -a ${LOGFILE}
# fi
# ${CP} installer/delete_duplicate_preventive.bsh ${INSTALLER_DIR}/delete_duplicate_preventive.bsh  | ${TEE} -a ${LOGFILE}
# ${CHMOD} 750 ${INSTALLER_DIR}/delete_duplicate_preventive.bsh  | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/extract_BO.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/extract_BO.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/extract_BO.bsh ${INSTALLER_DIR}/extract_BO.bsh  | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/extract_BO.bsh  | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/extract_reports.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/extract_reports.bsh| ${TEE} -a ${LOGFILE}
fi
${CP} installer/extract_reports.bsh ${INSTALLER_DIR}/extract_reports.bsh  | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/extract_reports.bsh  | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/remove_techpack.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/remove_techpack.bsh| ${TEE} -a ${LOGFILE}
fi
${CP} installer/remove_techpack.bsh ${INSTALLER_DIR}/remove_techpack.bsh  | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/remove_techpack.bsh  | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/remove_techpack_from_dwhrep.sql ] ; then
  ${RM} -f ${INSTALLER_DIR}/remove_techpack_from_dwhrep.sql| ${TEE} -a ${LOGFILE}
fi
${CP} installer/remove_techpack_from_dwhrep.sql ${INSTALLER_DIR}/remove_techpack_from_dwhrep.sql  | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/remove_techpack_from_dwhrep.sql  | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/remove_techpack_from_etlrep.sql ] ; then
  ${RM} -f ${INSTALLER_DIR}/remove_techpack_from_etlrep.sql| ${TEE} -a ${LOGFILE}
fi
${CP} installer/remove_techpack_from_etlrep.sql ${INSTALLER_DIR}/remove_techpack_from_etlrep.sql  | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/remove_techpack_from_etlrep.sql  | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/insane_WA.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/insane_WA.bsh| ${TEE} -a ${LOGFILE}
fi
${CP} installer/insane_WA.bsh ${INSTALLER_DIR}/insane_WA.bsh  | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/insane_WA.bsh  | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/update_partition_plan.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/update_partition_plan.bsh| ${TEE} -a ${LOGFILE}
fi
${CP} installer/update_partition_plan.bsh ${INSTALLER_DIR}/update_partition_plan.bsh  | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/update_partition_plan.bsh  | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/default_plan.sql ] ; then
  ${RM} -f ${INSTALLER_DIR}/default_plan.sql| ${TEE} -a ${LOGFILE}
fi
${CP} installer/default_plan.sql ${INSTALLER_DIR}/default_plan.sql  | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/default_plan.sql  | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/limited_plan.sql ] ; then
  ${RM} -f ${INSTALLER_DIR}/limited_plan.sql| ${TEE} -a ${LOGFILE}
fi
${CP} installer/limited_plan.sql ${INSTALLER_DIR}/limited_plan.sql  | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/limited_plan.sql  | ${TEE} -a ${LOGFILE}

if [ -f ${INSTALLER_DIR}/dropERBSView.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/dropERBSView.bsh | ${TEE} -a ${LOGFILE}
fi

if [ -f ${INSTALLER_DIR}/dropRBSView.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/dropRBSView.bsh | ${TEE} -a ${LOGFILE}
fi

SERVER_TYPE=`${CAT} ${CONF_DIR}/niq.ini | ${GREP} -i server_type | $NAWK -F"=" '{print $2}'`

if [ "$SERVER_TYPE" != "stats" ]; then

	#Adding all the test harness artefacts
	if [ ! -d ${INSTALLER_DIR}/test_harness ] ; then
		${MKDIR} ${INSTALLER_DIR}/test_harness | ${TEE} -a ${LOGFILE}
	fi
	if [ ${VERBOSE} = 1 ] ; then
		${ECHO} "Installing test_tp_installer.bsh" | ${TEE} -a ${LOGFILE}
	fi
	if [ -f ${INSTALLER_DIR}/test_harness/test_tp_installer.bsh ] ; then
		${RM} -f ${INSTALLER_DIR}/test_harness/test_tp_installer.bsh | ${TEE} -a ${LOGFILE}
	fi
	${CP} installer/test_harness/test_tp_installer.bsh ${INSTALLER_DIR}/test_harness/test_tp_installer.bsh | ${TEE} -a ${LOGFILE}
	${CHMOD} 540 ${INSTALLER_DIR}/test_harness/test_tp_installer.bsh | ${TEE} -a ${LOGFILE}

	if [ ${VERBOSE} = 1 ] ; then	
		${ECHO} "Installing test_config_stats" | ${TEE} -a ${LOGFILE}
	fi
	if [ -f ${INSTALLER_DIR}/test_harness/test_config_stats ] ; then
		${RM} -f ${INSTALLER_DIR}/test_harness/test_config_stats | ${TEE} -a ${LOGFILE}
	fi
	${CP} installer/test_harness/test_config_stats ${INSTALLER_DIR}/test_harness/test_config_stats | ${TEE} -a ${LOGFILE}
	${CHMOD} 440 ${INSTALLER_DIR}/test_harness/test_config_stats | ${TEE} -a ${LOGFILE}

	if [ ${VERBOSE} = 1 ] ; then
		${ECHO} "Installing mock_output_svcs-a_controlzoneDifferent" | ${TEE} -a ${LOGFILE}
	fi
	if [ -f ${INSTALLER_DIR}/test_harness/mock_output_svcs-a_controlzoneDifferent ] ; then
		${RM} -f ${INSTALLER_DIR}/test_harness/mock_output_svcs-a_controlzoneDifferent | ${TEE} -a ${LOGFILE}
	fi
	${CP} installer/test_harness/mock_output_svcs-a_controlzoneDifferent ${INSTALLER_DIR}/test_harness/mock_output_svcs-a_controlzoneDifferent | ${TEE} -a ${LOGFILE}
	${CHMOD} 440 ${INSTALLER_DIR}/test_harness/mock_output_svcs-a_controlzoneDifferent | ${TEE} -a ${LOGFILE}

	if [ ${VERBOSE} = 1 ] ; then
		${ECHO} "Installing mock_output_svcs-a" | ${TEE} -a ${LOGFILE}
	fi
	if [ -f ${INSTALLER_DIR}/test_harness/mock_output_svcs-a ] ; then
		${RM} -f ${INSTALLER_DIR}/test_harness/mock_output_svcs-a | ${TEE} -a ${LOGFILE}
	fi
	${CP} installer/test_harness/mock_output_svcs-a ${INSTALLER_DIR}/test_harness/mock_output_svcs-a | ${TEE} -a ${LOGFILE}
	${CHMOD} 440 ${INSTALLER_DIR}/test_harness/mock_output_svcs-a | ${TEE} -a ${LOGFILE}

	if [ ${VERBOSE} = 1 ] ; then
		${ECHO} "Installing mock_output_getActiveInterfaces" | ${TEE} -a ${LOGFILE}
	fi
	if [ -f ${INSTALLER_DIR}/test_harness/mock_output_getActiveInterfaces ] ; then
		${RM} -f ${INSTALLER_DIR}/test_harness/mock_output_getActiveInterfaces | ${TEE} -a ${LOGFILE}
	fi
	${CP} installer/test_harness/mock_output_getActiveInterfaces ${INSTALLER_DIR}/test_harness/mock_output_getActiveInterfaces | ${TEE} -a ${LOGFILE}
	${CHMOD} 440 ${INSTALLER_DIR}/test_harness/mock_output_getActiveInterfaces | ${TEE} -a ${LOGFILE}

	if [ ${VERBOSE} = 1 ] ; then
		${ECHO} "Installing dependency_mocks.lib" | ${TEE} -a ${LOGFILE}
	fi
	if [ -f ${INSTALLER_DIR}/test_harness/dependency_mocks.lib ] ; then
		${RM} -f ${INSTALLER_DIR}/test_harness/dependency_mocks.lib | ${TEE} -a ${LOGFILE}
	fi
	${CP} installer/test_harness/dependency_mocks.lib ${INSTALLER_DIR}/test_harness/dependency_mocks.lib | ${TEE} -a ${LOGFILE}
	${CHMOD} 540 ${INSTALLER_DIR}/test_harness/dependency_mocks.lib | ${TEE} -a ${LOGFILE}

	if [ ${VERBOSE} = 1 ] ; then
		${ECHO} "Installing dependency_calls.lib" | ${TEE} -a ${LOGFILE}
	fi
	if [ -f ${INSTALLER_DIR}/test_harness/dependency_calls.lib ] ; then
		${RM} -f ${INSTALLER_DIR}/test_harness/dependency_calls.lib | ${TEE} -a ${LOGFILE}
	fi
	${CP} installer/test_harness/dependency_calls.lib ${INSTALLER_DIR}/test_harness/dependency_calls.lib | ${TEE} -a ${LOGFILE}
	${CHMOD} 540 ${INSTALLER_DIR}/test_harness/dependency_calls.lib | ${TEE} -a ${LOGFILE}

	if [ ${VERBOSE} = 1 ] ; then
		${ECHO} "Installing dep_expect_mtas_fail" | ${TEE} -a ${LOGFILE}
	fi
	if [ -f ${INSTALLER_DIR}/test_harness/dep_expect_mtas_fail ] ; then
		${RM} -f ${INSTALLER_DIR}/test_harness/dep_expect_mtas_fail | ${TEE} -a ${LOGFILE}
	fi
	${CP} installer/test_harness/dep_expect_mtas_fail ${INSTALLER_DIR}/test_harness/dep_expect_mtas_fail | ${TEE} -a ${LOGFILE}
	${CHMOD} 440 ${INSTALLER_DIR}/test_harness/dep_expect_mtas_fail | ${TEE} -a ${LOGFILE}

	if [ ${VERBOSE} = 1 ] ; then
		${ECHO} "Installing dep_expect_mtas" | ${TEE} -a ${LOGFILE}
	fi
	if [ -f ${INSTALLER_DIR}/test_harness/dep_expect_mtas ] ; then
		${RM} -f ${INSTALLER_DIR}/test_harness/dep_expect_mtas | ${TEE} -a ${LOGFILE}
	fi
	${CP} installer/test_harness/dep_expect_mtas ${INSTALLER_DIR}/test_harness/dep_expect_mtas | ${TEE} -a ${LOGFILE}
	${CHMOD} 440 ${INSTALLER_DIR}/test_harness/dep_expect_mtas | ${TEE} -a ${LOGFILE}

	if [ ${VERBOSE} = 1 ] ; then
		${ECHO} "Installing dep_expect_ltecfa_fail" | ${TEE} -a ${LOGFILE}
	fi
	if [ -f ${INSTALLER_DIR}/test_harness/dep_expect_ltecfa_fail ] ; then
		${RM} -f ${INSTALLER_DIR}/test_harness/dep_expect_ltecfa_fail | ${TEE} -a ${LOGFILE}
	fi
	${CP} installer/test_harness/dep_expect_ltecfa_fail ${INSTALLER_DIR}/test_harness/dep_expect_ltecfa_fail | ${TEE} -a ${LOGFILE}
	${CHMOD} 440 ${INSTALLER_DIR}/test_harness/dep_expect_ltecfa_fail | ${TEE} -a ${LOGFILE}

	if [ ${VERBOSE} = 1 ] ; then
		${ECHO} "Installing dep_expect_ltecfa" | ${TEE} -a ${LOGFILE}
	fi
	if [ -f ${INSTALLER_DIR}/test_harness/dep_expect_ltecfa ] ; then
		${RM} -f ${INSTALLER_DIR}/test_harness/dep_expect_ltecfa | ${TEE} -a ${LOGFILE}
	fi
	${CP} installer/test_harness/dep_expect_ltecfa ${INSTALLER_DIR}/test_harness/dep_expect_ltecfa | ${TEE} -a ${LOGFILE}
	${CHMOD} 440 ${INSTALLER_DIR}/test_harness/dep_expect_ltecfa | ${TEE} -a ${LOGFILE}

	if [ ${VERBOSE} = 1 ] ; then
		${ECHO} "Installing cxc_num_mtas" | ${TEE} -a ${LOGFILE}
	fi
	if [ -f ${INSTALLER_DIR}/test_harness/cxc_num_mtas ] ; then
		${RM} -f ${INSTALLER_DIR}/test_harness/cxc_num_mtas | ${TEE} -a ${LOGFILE}
	fi
	${CP} installer/test_harness/cxc_num_mtas ${INSTALLER_DIR}/test_harness/cxc_num_mtas | ${TEE} -a ${LOGFILE}
	${CHMOD} 440 ${INSTALLER_DIR}/test_harness/cxc_num_mtas | ${TEE} -a ${LOGFILE}

	if [ ${VERBOSE} = 1 ] ; then
		${ECHO} "Installing cxc_num_ltecfa" | ${TEE} -a ${LOGFILE}
	fi
	if [ -f ${INSTALLER_DIR}/test_harness/cxc_num_ltecfa ] ; then
		${RM} -f ${INSTALLER_DIR}/test_harness/cxc_num_ltecfa | ${TEE} -a ${LOGFILE}
	fi
	${CP} installer/test_harness/cxc_num_ltecfa ${INSTALLER_DIR}/test_harness/cxc_num_ltecfa | ${TEE} -a ${LOGFILE}
	${CHMOD} 440 ${INSTALLER_DIR}/test_harness/cxc_num_ltecfa | ${TEE} -a ${LOGFILE}

	##And the dummy tech packs for the test harness
	if [ ! -d ${INSTALLER_DIR}/test_harness/dummy_techpacks ] ; then
		${MKDIR} ${INSTALLER_DIR}/test_harness/dummy_techpacks | ${TEE} -a ${LOGFILE}
	fi
	dummyTechPack=DIM_E_IMSI_IMEI_R3A_b31.tpi
	if [ ${VERBOSE} = 1 ] ; then
		${ECHO} "Installing ${dummyTechPack}" | ${TEE} -a ${LOGFILE}
	fi
	if [ -f ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} ] ; then
		${RM} -f ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}
	fi
	${CP} installer/test_harness/dummy_techpacks/${dummyTechPack} ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}
	${CHMOD} 740 ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}

	dummyTechPack=DIM_E_IMSI_MSISDN_R6A_b26.tpi
	if [ ${VERBOSE} = 1 ] ; then
		${ECHO} "Installing ${dummyTechPack}" | ${TEE} -a ${LOGFILE}
	fi
	if [ -f ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} ] ; then
		${RM} -f ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}
	fi
	${CP} installer/test_harness/dummy_techpacks/${dummyTechPack} ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}
	${CHMOD} 740 ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}

	dummyTechPack=DIM_E_LTE_CFA_R4B_b24.tpi
	if [ ${VERBOSE} = 1 ] ; then
		${ECHO} "Installing ${dummyTechPack}" | ${TEE} -a ${LOGFILE}
	fi
	if [ -f ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} ] ; then
		${RM} -f ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}
	fi
	${CP} installer/test_harness/dummy_techpacks/${dummyTechPack} ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}
	${CHMOD} 740 ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}

	dummyTechPack=DIM_E_LTE_R8D_b133.tpi
	if [ ${VERBOSE} = 1 ] ; then
		${ECHO} "Installing ${dummyTechPack}" | ${TEE} -a ${LOGFILE}
	fi
	if [ -f ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} ] ; then
		${RM} -f ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}
	fi
	${CP} installer/test_harness/dummy_techpacks/${dummyTechPack} ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}
	${CHMOD} 740 ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}

	dummyTechPack=DIM_E_SGEH_R8D_b179.tpi
	if [ ${VERBOSE} = 1 ] ; then
		${ECHO} "Installing ${dummyTechPack}" | ${TEE} -a ${LOGFILE}
	fi
	if [ -f ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} ] ; then
		${RM} -f ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}
	fi
	${CP} installer/test_harness/dummy_techpacks/${dummyTechPack} ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}
	${CHMOD} 740 ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}

	dummyTechPack=DWH_BASE_R8A_b83.tpi
	if [ ${VERBOSE} = 1 ] ; then
		${ECHO} "Installing ${dummyTechPack}" | ${TEE} -a ${LOGFILE}
	fi
	if [ -f ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} ] ; then
		${RM} -f ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}
	fi
	${CP} installer/test_harness/dummy_techpacks/${dummyTechPack} ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}
	${CHMOD} 740 ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}

	dummyTechPack=DWH_MONITOR_R12A_b104.tpi
	if [ ${VERBOSE} = 1 ] ; then
		${ECHO} "Installing ${dummyTechPack}" | ${TEE} -a ${LOGFILE}
	fi
	if [ -f ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} ] ; then
		${RM} -f ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}
	fi
	${CP} installer/test_harness/dummy_techpacks/${dummyTechPack} ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}
	${CHMOD} 740 ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}

	dummyTechPack=EVENT_E_LTE_CFA_R3A_b31.tpi
	if [ ${VERBOSE} = 1 ] ; then
		${ECHO} "Installing ${dummyTechPack}" | ${TEE} -a ${LOGFILE}
	fi
	if [ -f ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} ] ; then
		${RM} -f ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}
	fi
	${CP} installer/test_harness/dummy_techpacks/${dummyTechPack} ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}
	${CHMOD} 740 ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}

	dummyTechPack=EVENTS_DWH_BASE_R3A_b31.tpi
	if [ ${VERBOSE} = 1 ] ; then
		${ECHO} "Installing ${dummyTechPack}" | ${TEE} -a ${LOGFILE}
	fi
	if [ -f ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} ] ; then
		${RM} -f ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}
	fi
	${CP} installer/test_harness/dummy_techpacks/${dummyTechPack} ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}
	${CHMOD} 740 ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}

	dummyTechPack=GROUP_TYPE_E_R6A_b39.tpi
	if [ ${VERBOSE} = 1 ] ; then
		${ECHO} "Installing ${dummyTechPack}" | ${TEE} -a ${LOGFILE}
	fi
	if [ -f ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} ] ; then
		${RM} -f ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}
	fi
	${CP} installer/test_harness/dummy_techpacks/${dummyTechPack} ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}
	${CHMOD} 740 ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}

	dummyTechPack=INTF_DIM_E_LTE_ERBS_R1C_b3.tpi
	if [ ${VERBOSE} = 1 ] ; then
		${ECHO} "Installing ${dummyTechPack}" | ${TEE} -a ${LOGFILE}
	fi
	if [ -f ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} ] ; then
		${RM} -f ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}
	fi
	${CP} installer/test_harness/dummy_techpacks/${dummyTechPack} ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}
	${CHMOD} 740 ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}

	dummyTechPack=INTF_DIM_E_SGEH_3G_R3A_b15.tpi
	if [ ${VERBOSE} = 1 ] ; then
		${ECHO} "Installing ${dummyTechPack}" | ${TEE} -a ${LOGFILE}
	fi
	if [ -f ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} ] ; then
		${RM} -f ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}
	fi
	${CP} installer/test_harness/dummy_techpacks/${dummyTechPack} ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}
	${CHMOD} 740 ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}

	dummyTechPack=INTF_DIM_E_SGEH_R3A_b15.tpi
	if [ ${VERBOSE} = 1 ] ; then
		${ECHO} "Installing ${dummyTechPack}" | ${TEE} -a ${LOGFILE}
	fi
	if [ -f ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} ] ; then
		${RM} -f ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}
	fi
	${CP} installer/test_harness/dummy_techpacks/${dummyTechPack} ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}
	${CHMOD} 740 ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}

	dummyTechPack=M_E_LTEEFA_R5A_b121.tpi
	if [ ${VERBOSE} = 1 ] ; then
		${ECHO} "Installing ${dummyTechPack}" | ${TEE} -a ${LOGFILE}
	fi
	if [ -f ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} ] ; then
		${RM} -f ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}
	fi
	${CP} installer/test_harness/dummy_techpacks/${dummyTechPack} ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}
	${CHMOD} 740 ${INSTALLER_DIR}/test_harness/dummy_techpacks/${dummyTechPack} | ${TEE} -a ${LOGFILE}

else 
	${ECHO} "Skipping Test Harness artefacts on $SERVER_TYPE"
 fi
 
# ---------------------------------------------------------------------
# Adding hosts_file_update.sh to automate hosts file updation
# ---------------------------------------------------------------------

if [ -f ${INSTALLER_DIR}/hosts_file_update.sh ] ; then
  ${RM} -f ${INSTALLER_DIR}/hosts_file_update.sh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/hosts_file_update.sh ${INSTALLER_DIR}/hosts_file_update.sh | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/hosts_file_update.sh | ${TEE} -a ${LOGFILE}

# ---------------------------------------------------------------------
# Adding retrieve_from_db.sh to retrieve alarm password from database
#fix for EQEV-30561 EQEV-30684
# ---------------------------------------------------------------------

if [ -f ${INSTALLER_DIR}/retrieve_from_db.sh ] ; then
  ${RM} -f ${INSTALLER_DIR}/retrieve_from_db.sh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/retrieve_from_db.sh ${INSTALLER_DIR}/retrieve_from_db.sh | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/retrieve_from_db.sh | ${TEE} -a ${LOGFILE}


# ---------------------------------------------------------------------
# Adding store_to_db.sh to store the password to database
#fix for EQEV-30561 EQEV-30684
# ---------------------------------------------------------------------

if [ -f ${INSTALLER_DIR}/store_to_db.sh ] ; then
  ${RM} -f ${INSTALLER_DIR}/store_to_db.sh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/store_to_db.sh ${INSTALLER_DIR}/store_to_db.sh | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/store_to_db.sh | ${TEE} -a ${LOGFILE}


# ---------------------------------------------------------------------
# Adding manage_alarm_reports.sh
#fix for EQEV-33129
# ---------------------------------------------------------------------

if [ -f ${INSTALLER_DIR}/manage_alarm_reports.sh ] ; then
  ${RM} -f ${INSTALLER_DIR}/manage_alarm_reports.sh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/manage_alarm_reports.sh ${INSTALLER_DIR}/manage_alarm_reports.sh | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/manage_alarm_reports.sh | ${TEE} -a ${LOGFILE}



# ---------------------------------------------------------------------
# Adding LogRetention.bsh
#fix for EQEV-56797
# ---------------------------------------------------------------------

if [ -f ${INSTALLER_DIR}/LogRetention.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/LogRetention.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/LogRetention.bsh ${INSTALLER_DIR}/LogRetention.bsh | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/LogRetention.bsh | ${TEE} -a ${LOGFILE}


# ---------------------------------------------------------------------

#fix for EQEV-70185 Change in script name and added logic to get the number of sessions
# ---------------------------------------------------------------------

if [ -f ${INSTALLER_DIR}/change_session_properties.sh ] ; then
  ${RM} -f ${INSTALLER_DIR}/change_session_properties.sh | ${TEE} -a ${LOGFILE}
fi

if [ -f ${INSTALLER_DIR}/adminui_sessions.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/adminui_sessions.bsh | ${TEE} -a ${LOGFILE}
fi

${CP} installer/adminui_sessions.bsh ${INSTALLER_DIR}/adminui_sessions.bsh | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/adminui_sessions.bsh | ${TEE} -a ${LOGFILE}



# ---------------------------------------------------------------------
# Adding getPassword.bsh
#fix for EQEV-68047
# ---------------------------------------------------------------------

if [ -f ${INSTALLER_DIR}/getPassword.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/getPassword.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/getPassword.bsh ${INSTALLER_DIR}/getPassword.bsh | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/getPassword.bsh | ${TEE} -a ${LOGFILE}



# ---------------------------------------------------------------------
# Adding manage_tomcat_user.bsh
#fix for EQEV-69284
# ---------------------------------------------------------------------

if [ -f ${INSTALLER_DIR}/manage_tomcat_user.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/manage_tomcat_user.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/manage_tomcat_user.bsh ${INSTALLER_DIR}/manage_tomcat_user.bsh | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/manage_tomcat_user.bsh | ${TEE} -a ${LOGFILE}



# ---------------------------------------------------------------------
# Adding node_type_granularity.bsh
#Impl for EQEV-64304
# ---------------------------------------------------------------------

if [ -f ${INSTALLER_DIR}/node_type_granularity.bsh ] ; then
  ${RM} -f ${INSTALLER_DIR}/node_type_granularity.bsh | ${TEE} -a ${LOGFILE}
fi
${CP} installer/node_type_granularity.bsh ${INSTALLER_DIR}/node_type_granularity.bsh | ${TEE} -a ${LOGFILE}
${CHMOD} 740 ${INSTALLER_DIR}/node_type_granularity.bsh | ${TEE} -a ${LOGFILE}



# ---------------------------------------------------------------------
# Update or create versiondb.properties
# ---------------------------------------------------------------------

if [ ${VERBOSE} = 1 ] ; then
  ${ECHO} "Updating version database..." | ${TEE} -a ${LOGFILE}
fi

TVER=`${CAT} install/version.properties | ${GREP} module.version`
TBLD=`${CAT} install/version.properties | ${GREP} module.build`

VER=${TVER##*=}
BLD=${TBLD##*=}

VTAG="module.installer=${VER}b${BLD}"

if [ ! -f ${INSTALLER_DIR}/versiondb.properties ] ; then

  ${ECHO} "${VTAG}" > ${INSTALLER_DIR}/versiondb.properties
  ${CHMOD} 640 ${INSTALLER_DIR}/versiondb.properties

else

  OLD=`${CAT} ${INSTALLER_DIR}/versiondb.properties | ${GREP} module.installer`

  if [ -z "${OLD}" ] ; then
    ${ECHO} "${VTAG}" >> ${INSTALLER_DIR}/versiondb.properties
  else
    ${CP} ${INSTALLER_DIR}/versiondb.properties ${INSTALLER_DIR}/versiondb.properties.tmp
    ${SED} -e "/${OLD}/s//${VTAG}/g" ${INSTALLER_DIR}/versiondb.properties.tmp > ${INSTALLER_DIR}/versiondb.properties
    ${RM} ${INSTALLER_DIR}/versiondb.properties.tmp
  fi

fi

if [ $VERBOSE = 1 ] ; then
  ${ECHO} "Installer installed" | ${TEE} -a ${LOGFILE}
fi

${ECHO} "Encrypting the KeyStore Password." | ${TEE} -a ${LOGFILE}
${INSTALLER_DIR}/configure_newkeystore.sh -p | ${TEE} -a ${LOGFILE}

exit 0


