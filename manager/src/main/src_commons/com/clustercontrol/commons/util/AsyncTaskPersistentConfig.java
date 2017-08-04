/*

Copyright (C) 2010 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.commons.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;

public class AsyncTaskPersistentConfig {

	private static Log log = LogFactory.getLog(AsyncTaskPersistentConfig.class);

	/**
	 * 該当のモジュール種別からの出力結果を永続化させるかどうかを返します。
	 */
	public static boolean isPersisted(String typeId) {
		boolean ret = true;

		if ( HinemosModuleConstant.MONITOR_AGENT.equals(typeId) ) {
			ret = getConfig("worker.async.task.persist.monitor.agent", false);
		} else if ( HinemosModuleConstant.MONITOR_CUSTOM_N.equals(typeId)
				||  HinemosModuleConstant.MONITOR_CUSTOM_S.equals(typeId)) {
			ret = getConfig("worker.async.task.persist.monitor.custom", false);
		} else if (HinemosModuleConstant.MONITOR_HTTP_N.equals(typeId)
				|| HinemosModuleConstant.MONITOR_HTTP_S.equals(typeId)) {
			ret = getConfig("worker.async.task.persist.monitor.http", false);
		} else if ( HinemosModuleConstant.MONITOR_PERFORMANCE.equals(typeId) ) {
			ret = getConfig("worker.async.task.persist.monitor.performance", false);
		} else if ( HinemosModuleConstant.MONITOR_PING.equals(typeId) ) {
			ret = getConfig("worker.async.task.persist.monitor.ping", false);
		} else if ( HinemosModuleConstant.MONITOR_PORT.equals(typeId) ) {
			ret = getConfig("worker.async.task.persist.monitor.port", false);
		} else if ( HinemosModuleConstant.MONITOR_PROCESS.equals(typeId) ) {
			ret = getConfig("worker.async.task.persist.monitor.process", false);
		} else if ( HinemosModuleConstant.MONITOR_SNMPTRAP.equals(typeId) ) {
			ret = getConfig("worker.async.task.persist.monitor.snmptrap", false);
		} else if (HinemosModuleConstant.MONITOR_SNMP_N.equals(typeId)
				|| HinemosModuleConstant.MONITOR_SNMP_S.equals(typeId)) {
			ret = getConfig("worker.async.task.persist.monitor.snmp", false);
		} else if (HinemosModuleConstant.MONITOR_SQL_N.equals(typeId)
				|| HinemosModuleConstant.MONITOR_SQL_S.equals(typeId)) {
			ret = getConfig("worker.async.task.persist.monitor.sql", false);
		} else if ( HinemosModuleConstant.MONITOR_SYSTEMLOG.equals(typeId) ) {
			ret = getConfig("worker.async.task.persist.monitor.syslog", false);
		} else if ( HinemosModuleConstant.MONITOR_LOGFILE.equals(typeId) ) {
			ret = getConfig("worker.async.task.persist.monitor.logfile", false);
		} else if ( HinemosModuleConstant.JOB.equals(typeId) ) {
			ret = getConfig("worker.async.task.persist.job", true);
		} else if ( HinemosModuleConstant.SYSYTEM_MAINTENANCE.equals(typeId) ) {
			ret = getConfig("worker.async.task.persist.maintenance", true);
		} else if ( HinemosModuleConstant.MONITOR_WINEVENT.equals(typeId) ) {
			ret = getConfig("worker.async.task.persist.monitor.winevent", true);
		} else if ( HinemosModuleConstant.MONITOR_CUSTOMTRAP_N.equals(typeId)
				||  HinemosModuleConstant.MONITOR_CUSTOMTRAP_S.equals(typeId)) {
			ret = getConfig("worker.async.task.persist.monitor.customtrap", false);
		}

		return ret;
	}

	private static boolean getConfig(String key, boolean def) {
		boolean ret = def;

		// プロパティファイルから該当するキーの値を読み込む
		ret = HinemosPropertyUtil.getHinemosPropertyBool(key, def);
		log.debug("initialized async task persistency (" + key + ") :" + ret);

		return ret;
	}
}
