/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.commons.util;

import com.clustercontrol.bean.HinemosModuleConstant;

public class AsyncTaskPersistentConfig {

	/**
	 * 該当のモジュール種別からの出力結果を永続化させるかどうかを返します。
	 */
	public static boolean isPersisted(String typeId) {
		boolean ret = true;

		if ( HinemosModuleConstant.MONITOR_AGENT.equals(typeId) ) {
			ret = HinemosPropertyCommon.worker_async_task_persist_monitor_agent.getBooleanValue();
		} else if ( HinemosModuleConstant.MONITOR_CUSTOM_N.equals(typeId)
				||  HinemosModuleConstant.MONITOR_CUSTOM_S.equals(typeId)) {
			ret = HinemosPropertyCommon.worker_async_task_persist_monitor_custom.getBooleanValue();
		} else if (HinemosModuleConstant.MONITOR_HTTP_N.equals(typeId)
				|| HinemosModuleConstant.MONITOR_HTTP_S.equals(typeId)) {
			ret = HinemosPropertyCommon.worker_async_task_persist_monitor_http.getBooleanValue();
		} else if ( HinemosModuleConstant.MONITOR_PERFORMANCE.equals(typeId) ) {
			ret = HinemosPropertyCommon.worker_async_task_persist_monitor_performance.getBooleanValue();
		} else if ( HinemosModuleConstant.MONITOR_PING.equals(typeId) ) {
			ret = HinemosPropertyCommon.worker_async_task_persist_monitor_ping.getBooleanValue();
		} else if ( HinemosModuleConstant.MONITOR_PORT.equals(typeId) ) {
			ret = HinemosPropertyCommon.worker_async_task_persist_monitor_port.getBooleanValue();
		} else if ( HinemosModuleConstant.MONITOR_PROCESS.equals(typeId) ) {
			ret = HinemosPropertyCommon.worker_async_task_persist_monitor_process.getBooleanValue();
		} else if ( HinemosModuleConstant.MONITOR_SNMPTRAP.equals(typeId) ) {
			ret = HinemosPropertyCommon.worker_async_task_persist_monitor_snmptrap.getBooleanValue();
		} else if (HinemosModuleConstant.MONITOR_SNMP_N.equals(typeId)
				|| HinemosModuleConstant.MONITOR_SNMP_S.equals(typeId)) {
			ret = HinemosPropertyCommon.worker_async_task_persist_monitor_snmp.getBooleanValue();
		} else if (HinemosModuleConstant.MONITOR_SQL_N.equals(typeId)
				|| HinemosModuleConstant.MONITOR_SQL_S.equals(typeId)) {
			ret = HinemosPropertyCommon.worker_async_task_persist_monitor_sql.getBooleanValue();
		} else if ( HinemosModuleConstant.MONITOR_SYSTEMLOG.equals(typeId) ) {
			ret = HinemosPropertyCommon.worker_async_task_persist_monitor_syslog.getBooleanValue();
		} else if ( HinemosModuleConstant.MONITOR_LOGFILE.equals(typeId) ) {
			ret = HinemosPropertyCommon.worker_async_task_persist_monitor_logfile.getBooleanValue();
		} else if ( HinemosModuleConstant.MONITOR_LOGCOUNT.equals(typeId) ) {
			ret = HinemosPropertyCommon.worker_async_task_persist_monitor_logcount.getBooleanValue();
		} else if (HinemosModuleConstant.MONITOR_BINARYFILE_BIN.equals(typeId)) {
			ret = HinemosPropertyCommon.worker_async_task_persist_monitor_binaryfile.getBooleanValue();
		} else if (HinemosModuleConstant.MONITOR_PCAP_BIN.equals(typeId)) {
			ret = HinemosPropertyCommon.worker_async_task_persist_monitor_packetcapture.getBooleanValue();
		} else if ( HinemosModuleConstant.JOB.equals(typeId) ) {
			ret = HinemosPropertyCommon.worker_async_task_persist_job.getBooleanValue();
		} else if ( HinemosModuleConstant.SYSYTEM_MAINTENANCE.equals(typeId) ) {
			ret = HinemosPropertyCommon.worker_async_task_persist_maintenance.getBooleanValue();
		} else if ( HinemosModuleConstant.MONITOR_WINEVENT.equals(typeId) ) {
			ret = HinemosPropertyCommon.worker_async_task_persist_monitor_winevent.getBooleanValue();
		} else if ( HinemosModuleConstant.MONITOR_CUSTOMTRAP_N.equals(typeId)
				||  HinemosModuleConstant.MONITOR_CUSTOMTRAP_S.equals(typeId)) {
			ret = HinemosPropertyCommon.worker_async_task_persist_monitor_customtrap.getBooleanValue();
		} else if ( HinemosModuleConstant.MONITOR_CORRELATION.equals(typeId) ) {
			ret = HinemosPropertyCommon.worker_async_task_persist_monitor_correlation.getBooleanValue();
		} else if ( HinemosModuleConstant.MONITOR_INTEGRATION.equals(typeId) ) {
			ret = HinemosPropertyCommon.worker_async_task_persist_monitor_integration.getBooleanValue();
		}

		return ret;
	}
}
