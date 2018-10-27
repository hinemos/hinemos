/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.vcloud.session;

import java.util.List;

import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.reporting.vcloud.factory.SelectReportingVCloud;


/**
*
* <!-- begin-user-doc --> AWSの課金情報の制御を行うsession bean <!-- end-user-doc --> *
*
*/
public class ReportingVCloudControllerBean {
	
	/**
	 * MonitorInfoの一覧を取得します。<BR>
	 * 
	 * @param itemFilter
	 * @param ownerRoleId
	 * @return
	 */
	public List<MonitorInfo> getMonitorInfoListByMonitorTypeId(String monitorId, String monitorTypeId) {
		SelectReportingVCloud select = new SelectReportingVCloud();
		List<MonitorInfo> list = select.getMonitorInfoListByMonitorTypeId(monitorId, monitorTypeId);
		return list;
	}
	
	/**
	 * monitor_idをsetting.cc_monitor_infoテーブルとsetting.cc_monitor_plugin_string_infoテーブルからmonitorKindを条件に取得します。<BR>
	 * 
	 * @param monitorId
	 * @param value
	 * @return
	 */
	public List<Object[]> getBillingMonitorIdByMonitorKind(String monitorId, String value) {
		SelectReportingVCloud select = new SelectReportingVCloud();
		List<Object[]> list = select.getBillingMonitorIdByMonitorKind(monitorId, value);
		return list;
	}
}
