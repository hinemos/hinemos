/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.analytics.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Combo;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.collect.util.CollectEndpointWrapper;
import com.clustercontrol.monitor.run.bean.MonitorTypeMessage;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.ws.collect.CollectKeyInfo;
import com.clustercontrol.ws.collect.HashMapInfo;
import com.clustercontrol.ws.collect.HinemosUnknown_Exception;
import com.clustercontrol.ws.collect.InvalidUserPass_Exception;
import com.clustercontrol.ws.collect.HashMapInfo.Map9;
import com.clustercontrol.ws.collect.HashMapInfo.Map9.Entry;
import com.clustercontrol.ws.monitor.MonitorInfo;

/**
 * サイレント障害系監視のユーティリティクラス
 *
 */
public class AnalyticsUtil {

	/**
	 * 収集値表示名コンボボックスを設定する(数値監視用)
	 * 
	 * @param combo 設定先のコンボボックス
	 * @param map 設定先のマップ
	 * @param facilityId ファシリティID
	 * @param managerName マネージャ名
	 * @param ownerRoleId オーナーロールID
	 * @param isNode true:ノードのみ、false：ノード・スコープ
	 */
	public static void setComboItemNameForNumeric(
			Combo combo, Map<String, CollectKeyInfo> map, 
			String facilityId, String managerName, String ownerRoleId) {
		combo.removeAll();
		map.clear();
		if (facilityId == null || facilityId.isEmpty()) {
			return;
		}
		try {
			CollectEndpointWrapper wrapper = CollectEndpointWrapper.getWrapper(managerName);
			HashMapInfo hashMapInfo = wrapper.getCollectKeyMapForAnalytics(
					facilityId, ownerRoleId);
			Map9 map9 = hashMapInfo.getMap9();
			if (map9 == null) {
				return;
			}
			for (Entry entry : map9.getEntry()) {
				String key = HinemosMessage.replace(entry.getKey());
				map.put(key, entry.getValue());
				combo.add(key);
			}
			// ソート
			String[] items = combo.getItems();
			Arrays.sort(items);
			combo.setItems(items);
		} catch (HinemosUnknown_Exception 
				| com.clustercontrol.ws.collect.InvalidRole_Exception
				| InvalidUserPass_Exception e) {
			// 何もしない
		}
	}

	/**
	 * 収集値表示名コンボボックスを設定する(文字列用)
	 * 
	 * @param combo 設定先のコンボボックス
	 * @param map 設定先のマップ
	 * @param facilityId ファシリティID
	 * @param managerName マネージャ名
	 * @param ownerRoleId オーナーロールID
	 */
	public static void setComboItemNameForString(
			Combo combo, Map<String, MonitorInfo> map, String facilityId, String managerName, String ownerRoleId) {
		combo.removeAll();
		if (facilityId == null || facilityId.isEmpty()) {
			return;
		}
		try {
			MonitorSettingEndpointWrapper wrapper = MonitorSettingEndpointWrapper.getWrapper(managerName);
			List<MonitorInfo> list = wrapper.getStringMonitoInfoListForAnalytics(facilityId, ownerRoleId);
			if (list == null) {
				return;
			}
			for (MonitorInfo monitorInfo : list) {
				String label = AnalyticsUtil.getMonitorIdLabel(monitorInfo);
				map.put(label, monitorInfo);
				combo.add(label);
			}
		} catch (com.clustercontrol.ws.monitor.HinemosUnknown_Exception
				| com.clustercontrol.ws.monitor.InvalidRole_Exception
				| com.clustercontrol.ws.monitor.InvalidUserPass_Exception e) {
			// 何もしない
		}
	}

	/**
	 * 収集値表示名を取得する(数値)
	 * 
	 * @param map 取得元マップ
	 * @param monitorId 監視項目ID
	 * @param displayName DisplayName
	 * @param itemName　ItemName
	 * @return　収集値表示名
	 */
	public static String getComboItemNameForNumeric(Map<String, CollectKeyInfo> map, 
			String monitorId, String displayName, String itemName) {
		String rtn = "";
		if (map == null 
				|| monitorId == null || monitorId.isEmpty()
				|| displayName == null
				|| itemName == null || itemName.isEmpty()) {
			return rtn;
		}
		for (Map.Entry<String, CollectKeyInfo> entry : map.entrySet()) {
			if (entry.getValue().getMonitorId().equals(monitorId)
					&& entry.getValue().getDisplayName().equals(displayName)
					&& entry.getValue().getItemName().equals(itemName)) {
				rtn = entry.getKey();
				break;
			}
		}
		return rtn;
	}

	/**
	 * 収集値表示名を取得する(文字列)
	 * 
	 * @param map 取得元マップ
	 * @param monitorId ファシリティID
	 * @return　収集値表示名
	 */
	public static String getComboItemNameForString(Map<String, MonitorInfo> map, 
			String monitorId) {
		String rtn = "";
		if (map == null 
				|| monitorId == null || monitorId.isEmpty()) {
			return rtn;
		}
		for (Map.Entry<String, MonitorInfo> entry : map.entrySet()) {
			if (entry.getValue().getMonitorId().equals(monitorId)) {
				rtn = entry.getKey();
				break;
			}
		}
		return rtn;
	}

	/**
	 * 監視設定文字列を取得する
	 * 
	 * @param monitorInfo 監視情報
	 * @return 監視設定文字列
	 */
	public static String getMonitorIdLabel(MonitorInfo monitorInfo) {
		String pluginName = "";
		if (monitorInfo == null || monitorInfo.getMonitorTypeId() == null) {
			pluginName = "";
			return null;
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_AGENT)) {
			pluginName = Messages.getString("agent.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_HTTP_N)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_HTTP_S)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_HTTP_SCENARIO)) {
			pluginName = Messages.getString("http.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_PERFORMANCE)) {
			pluginName = Messages.getString("performance.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_PING)) {
			pluginName = Messages.getString("ping.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_PORT)) {
			pluginName = Messages.getString("port.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_PROCESS)) {
			pluginName = Messages.getString("process.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_SNMP_N)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_SNMP_S)) {
			pluginName = Messages.getString("snmp.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_SQL_N)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_SQL_S)) {
			pluginName = Messages.getString("sql.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_SYSTEMLOG)) {
			pluginName = Messages.getString("systemlog.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_LOGFILE)) {
			pluginName = Messages.getString("logfile.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOM_N)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOM_S)) {
			pluginName = Messages.getString("custom.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_SNMPTRAP)) {
			pluginName = Messages.getString("snmptrap.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_WINSERVICE)) {
			pluginName = Messages.getString("winservice.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_WINEVENT)) {
			pluginName = Messages.getString("winevent.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_JMX)) {
			pluginName = Messages.getString("jmx.monitor");
		} else if (monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_N)
				|| monitorInfo.getMonitorTypeId().equals(HinemosModuleConstant.MONITOR_CUSTOMTRAP_S)) {
			pluginName = Messages.getString("customtrap.monitor");
		} else {
			pluginName = monitorInfo.getMonitorTypeId();
		}

		return String.format("%s (%s[%s])", 
				monitorInfo.getMonitorId(),
				pluginName, 
				MonitorTypeMessage.typeToString(monitorInfo.getMonitorType()));
	}
}
