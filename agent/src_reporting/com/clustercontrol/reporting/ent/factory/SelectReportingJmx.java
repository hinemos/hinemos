/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.ent.factory;

import java.util.List;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.jmx.model.JmxCheckInfo;
import com.clustercontrol.jmx.model.JmxMasterInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;

public class SelectReportingJmx {

	/**
	 * JmxCheckInfoを取得します。<BR>
	 * 
	 * @param monitorId
	 * @return
	 * @throws MonitorNotFound
	 */
	public JmxCheckInfo getMonitorJmxInfoPK(String monitorId) throws MonitorNotFound {
		return com.clustercontrol.reporting.ent.util.QueryUtil.getMonitorJmxInfoPK(monitorId, ObjectPrivilegeMode.NONE);
	}

	/**
	 * JmxCheckInfoを取得します。<BR>
	 * 
	 * @param facilityId
	 * @param masterId
	 * @return
	 * @throws FacilityNotFound
	 */
	public JmxCheckInfo getMonitorJmxInfo(String monitorId, String masterId) throws MonitorNotFound {
		return com.clustercontrol.reporting.ent.util.QueryUtil.getMonitorJmxInfoByMonitorIdAndMasterId(monitorId, null);
	}
	
	/**
	 * JmxMasterInfoを取得します。<BR>
	 * 
	 * @param masterId
	 * @return
	 * @throws MonitorNotFound
	 */
	public JmxMasterInfo getJmxMasterInfoPK(String masterId) throws MonitorNotFound {
		return com.clustercontrol.reporting.ent.util.QueryUtil.getJmxMasterInfoPK(masterId, ObjectPrivilegeMode.NONE);
	}
	
	/**
	 * MonitorInfoの一覧を取得します。<BR>
	 * 
	 * @param monitorTypeId
	 * @param ownerRoleId
	 * @return
	 */
	public List<MonitorInfo> getMonitorInfoByMonitorTypeId(String monitorTypeId, String ownerRoleId) {
		return com.clustercontrol.reporting.ent.util.QueryUtil.getMonitorInfoByMonitorTypeId(monitorTypeId, ownerRoleId);
	}
	
	/**
	 * MonitorInfoを全一覧を取得します。<BR>
	 * 
	 * @return
	 * @throws HinemosUnknown
	 */
	public List<MonitorInfo> getAllMonitorInfo() throws HinemosUnknown {
		return com.clustercontrol.reporting.ent.util.QueryUtil.getAllMonitorInfo();
	}
	
	/**
	 * CollectDataのAvgの合計値一覧を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param masterId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getSummarySumAvgData(String facilityId, Long fromTime, Long toTime, String masterId) throws HinemosDbTimeout {
		return com.clustercontrol.reporting.ent.util.QueryUtil.getSummaryJmxAvgData(facilityId, fromTime, toTime, masterId);
	}
	
	/**
	 * 時間毎のAvgの合計値一覧を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param masterId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getSummarySumAvgHour(String facilityId, Long fromTime, Long toTime, String masterId) throws HinemosDbTimeout {
		return com.clustercontrol.reporting.ent.util.QueryUtil.getSummaryJmxAvgHour(facilityId, fromTime, toTime, masterId);
	}
	
	/**
	 * 日別のAvgの合計値一覧を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param masterId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getSummarySumAvgDay(String facilityId, Long fromTime, Long toTime, String masterId) throws HinemosDbTimeout {
		return com.clustercontrol.reporting.ent.util.QueryUtil.getSummaryJmxAvgDay(facilityId, fromTime, toTime, masterId);
	}
	
	/**
	 * 月別のAvgの合計値一覧を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param masterId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getSummaryJmxAvgMonth(String facilityId, Long fromTime, Long toTime, String masterId) throws HinemosDbTimeout {
		return com.clustercontrol.reporting.ent.util.QueryUtil.getSummaryJmxAvgMonth(facilityId, fromTime, toTime, masterId);
	}
}
