/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.vcloud.factory;

import java.util.List;

import com.clustercontrol.monitor.run.model.MonitorInfo;

/**
 * AWSの課金情報を検索するクラス<BR>
 *
 * @version 6.0.a
 * @since 6.0.a
 */
public class SelectReportingVCloud {

	/**
	 * MonitorInfoの一覧をmonitorTypeIdを条件に取得します。<BR>
	 * 
	 * @param monitorTypeId
	 * @param excMonitorTypeId
	 * @param collectorFlg
	 * @param ownerRoleId
	 * @return
	 */
	public List<MonitorInfo> getMonitorInfoListByMonitorTypeId(String monitorId, String monitorTypeId) {
		return com.clustercontrol.reporting.vcloud.util.QueryUtil.getMonitorInfoByMonitorTypeId(monitorId, monitorTypeId);
	}
	
	/**
	 * monitor_idをsetting.cc_monitor_infoテーブルとsetting.cc_monitor_plugin_string_infoテーブルからmonitorKindを条件に取得します。<BR>
	 * 
	 * @param monitorId
	 * @param value
	 * @return
	 */
	public List<Object[]> getBillingMonitorIdByMonitorKind(String monitorId, String value) {
		return com.clustercontrol.reporting.vcloud.util.QueryUtil.getBillingMonitorIdByMonitorKind(monitorId, value);
	}
}