/*

Copyright (C) 2013 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.monitor.bean;

import java.util.ArrayList;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;

/**
 * 監視種別<BR>
 * 
 * @version 4.1.0
 * @since 1.0.0
 */
public class MonitorTypeMstConstant {

	private static ArrayList<ArrayList<Object>> monitorTypeMst =
			new ArrayList<ArrayList<Object>>();

	static {
		ArrayList<Object> list = null;
		list = new ArrayList<Object>();
		list.add(HinemosModuleConstant.MONITOR_AGENT);
		list.add(MonitorTypeConstant.TYPE_TRUTH);
		monitorTypeMst.add(list);
		list = new ArrayList<Object>();
		list.add(HinemosModuleConstant.MONITOR_HTTP_N);
		list.add(MonitorTypeConstant.TYPE_NUMERIC);
		monitorTypeMst.add(list);
		list = new ArrayList<Object>();
		list.add(HinemosModuleConstant.MONITOR_HTTP_S);
		list.add(MonitorTypeConstant.TYPE_STRING);
		monitorTypeMst.add(list);
		list = new ArrayList<Object>();
		list.add(HinemosModuleConstant.MONITOR_HTTP_SCENARIO);
		list.add(MonitorTypeConstant.TYPE_SCENARIO);  //MONITOR_TYPE_SCENARIO 
		monitorTypeMst.add(list);
		list = new ArrayList<Object>();
		list.add(HinemosModuleConstant.MONITOR_PING);
		list.add(MonitorTypeConstant.TYPE_NUMERIC);
		monitorTypeMst.add(list);
		list = new ArrayList<Object>();
		list.add(HinemosModuleConstant.MONITOR_SNMP_N);
		list.add(MonitorTypeConstant.TYPE_NUMERIC);
		monitorTypeMst.add(list);
		list = new ArrayList<Object>();
		list.add(HinemosModuleConstant.MONITOR_SNMP_S);
		list.add(MonitorTypeConstant.TYPE_STRING);
		monitorTypeMst.add(list);
		list = new ArrayList<Object>();
		list.add(HinemosModuleConstant.MONITOR_SNMPTRAP);
		list.add(MonitorTypeConstant.TYPE_TRAP);
		monitorTypeMst.add(list);
		list = new ArrayList<Object>();
		list.add(HinemosModuleConstant.MONITOR_SQL_N);
		list.add(MonitorTypeConstant.TYPE_NUMERIC);
		monitorTypeMst.add(list);
		list = new ArrayList<Object>();
		list.add(HinemosModuleConstant.MONITOR_SQL_S);
		list.add(MonitorTypeConstant.TYPE_STRING);
		monitorTypeMst.add(list);
		list = new ArrayList<Object>();
		list.add(HinemosModuleConstant.MONITOR_WINSERVICE);
		list.add(MonitorTypeConstant.TYPE_TRUTH);
		monitorTypeMst.add(list);
		list = new ArrayList<Object>();
		list.add(HinemosModuleConstant.MONITOR_WINEVENT);
		list.add(MonitorTypeConstant.TYPE_STRING);
		monitorTypeMst.add(list);
		list = new ArrayList<Object>();
		list.add(HinemosModuleConstant.MONITOR_CUSTOM_N);
		list.add(MonitorTypeConstant.TYPE_NUMERIC);
		monitorTypeMst.add(list);
		list = new ArrayList<Object>();
		list.add(HinemosModuleConstant.MONITOR_CUSTOM_S);
		list.add(MonitorTypeConstant.TYPE_STRING);
		monitorTypeMst.add(list);
		list = new ArrayList<Object>();
		list.add(HinemosModuleConstant.MONITOR_PORT);
		list.add(MonitorTypeConstant.TYPE_NUMERIC);
		monitorTypeMst.add(list);
		list = new ArrayList<Object>();
		list.add(HinemosModuleConstant.MONITOR_SYSTEMLOG);
		list.add(MonitorTypeConstant.TYPE_STRING);
		monitorTypeMst.add(list);
		list = new ArrayList<Object>();
		list.add(HinemosModuleConstant.MONITOR_PROCESS);
		list.add(MonitorTypeConstant.TYPE_NUMERIC);
		monitorTypeMst.add(list);
		list = new ArrayList<Object>();
		list.add(HinemosModuleConstant.MONITOR_PERFORMANCE);
		list.add(MonitorTypeConstant.TYPE_NUMERIC);
		monitorTypeMst.add(list);
		list = new ArrayList<Object>();
		list.add(HinemosModuleConstant.MONITOR_LOGFILE);
		list.add(MonitorTypeConstant.TYPE_STRING);
		monitorTypeMst.add(list);
		list = new ArrayList<Object>();
		list.add(HinemosModuleConstant.MONITOR_JMX);
		list.add(MonitorTypeConstant.TYPE_NUMERIC);
		monitorTypeMst.add(list);
		list = new ArrayList<Object>();
		list.add(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N);
		list.add(MonitorTypeConstant.TYPE_NUMERIC);
		monitorTypeMst.add(list);
		list = new ArrayList<Object>();
		list.add(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S);
		list.add(MonitorTypeConstant.TYPE_STRING);
		monitorTypeMst.add(list);
	}

	public static ArrayList<ArrayList<Object>> getListAll() {
		return monitorTypeMst;
	}
}