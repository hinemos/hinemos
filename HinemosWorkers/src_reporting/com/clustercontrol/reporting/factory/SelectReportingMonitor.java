/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.factory;

import java.util.List;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.fault.CollectorNotFound;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.performance.monitor.model.CollectorItemCodeMstEntity;
import com.clustercontrol.reporting.util.PriorityQueryUtil;

/**
 * 監視情報を検索する抽象クラス<BR>
 * <p>
 * 監視種別（真偽値，数値，文字列）の各クラスで継承してください。
 *
 * @version 4.1.0
 * @since 2.0.0
 */
public class SelectReportingMonitor {

	/**
	 * MonitorInfoを取得します。<BR>
	 * 
	 * @param monitorId
	 * @return
	 * @throws MonitorNotFound
	 */
	public MonitorInfo getMonitorInfo(String monitorId) throws MonitorNotFound {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getMonitorInfoPK(monitorId, ObjectPrivilegeMode.NONE);
	}
	
	/**
	 * MonitorInfoの一覧(時間単位)を取得します。<BR>
	 * 
	 * @param itemFilter
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param ownerRoleId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getMonitorHourList(String itemFilter, String facilityId, Long fromTime, Long toTime, String ownerRoleId) throws HinemosDbTimeout {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getMonitorHourList(itemFilter, facilityId, fromTime, toTime, ownerRoleId);
		
	}
	
	/**
	 * MonitorInfoの一覧(日単位)を取得します。<BR>
	 * 
	 * @param itemFilter
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param ownerRoleId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getMonitorDayList(String itemFilter, String facilityId, Long fromTime, Long toTime, String ownerRoleId) throws HinemosDbTimeout {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getMonitorDayList(itemFilter, facilityId, fromTime, toTime, ownerRoleId);
		
	}
	
	/**
	 * MonitorInfoの一覧(月単位)を取得します。<BR>
	 * 
	 * @param itemFilter
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param ownerRoleId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getMonitorMonthList(String itemFilter, String facilityId, Long fromTime, Long toTime, String ownerRoleId) throws HinemosDbTimeout {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getMonitorMonthList(itemFilter, facilityId, fromTime, toTime, ownerRoleId);
		
	}
	
	/**
	 * MonitorInfoの一覧(RAW)を取得します。<BR>
	 * 
	 * @param itemFilter
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param ownerRoleId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getMonitorDataList(String itemFilter, String facilityId, Long fromTime, Long toTime, String ownerRoleId) throws HinemosDbTimeout {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getMonitorDataList(itemFilter, facilityId, fromTime, toTime, ownerRoleId);
		
	}
	
	/**
	 * MonitorInfoの一覧(時間単位)をmonitorTypeIdを条件に取得します。<BR>
	 * 
	 * @param facilityId
	 * @param monitorId
	 * @param fromTime
	 * @param toTime
	 * @param ownerRoleId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getMonitorInfoHourList(String facilityId, String monitorId, Long fromTime, Long toTime, String ownerRoleId) throws HinemosDbTimeout {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getMonitorInfoHourList(facilityId, monitorId, fromTime, toTime, ownerRoleId);
	}
	
	/**
	 * MonitorInfoの一覧(時間単位)をmonitorTypeIdを条件に取得します。<BR>
	 * 
	 * @param facilityId
	 * @param monitorId
	 * @param fromTime
	 * @param toTime
	 * @param ownerRoleId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getMonitorInfoHourList(String facilityId, Boolean collectorFlg, Long fromTime, Long toTime, String ownerRoleId) throws HinemosDbTimeout {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getMonitorInfoHourList(facilityId, collectorFlg, fromTime, toTime, ownerRoleId);
	}
	
	/**
	 * MonitorInfoの一覧(日単位)をmonitorTypeIdを条件に取得します。<BR>
	 * 
	 * @param facilityId
	 * @param monitorId
	 * @param fromTime
	 * @param toTime
	 * @param ownerRoleId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getMonitorInfoDayList(String facilityId, String monitorId, Long fromTime, Long toTime, String ownerRoleId) throws HinemosDbTimeout {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getMonitorInfoDayList(facilityId, monitorId, fromTime, toTime, ownerRoleId);
	}
	
	/**
	 * MonitorInfoの一覧(日単位)をmonitorTypeIdを条件に取得します。<BR>
	 * 
	 * @param facilityId
	 * @param monitorId
	 * @param fromTime
	 * @param toTime
	 * @param ownerRoleId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getMonitorInfoDayList(String facilityId, Boolean collectorFlg, Long fromTime, Long toTime, String ownerRoleId) throws HinemosDbTimeout {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getMonitorInfoDayList(facilityId, collectorFlg, fromTime, toTime, ownerRoleId);
	}
	
	/**
	 * MonitorInfoの一覧(月単位)をmonitorTypeIdを条件に取得します。<BR>
	 * 
	 * @param facilityId
	 * @param monitorId
	 * @param fromTime
	 * @param toTime
	 * @param ownerRoleId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getMonitorInfoMonthList(String facilityId, String monitorId, Long fromTime, Long toTime, String ownerRoleId) throws HinemosDbTimeout {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getMonitorInfoMonthList(facilityId, monitorId, fromTime, toTime, ownerRoleId);
	}
	
	/**
	 * MonitorInfoの一覧(月単位)をmonitorTypeIdを条件に取得します。<BR>
	 * 
	 * @param facilityId
	 * @param monitorId
	 * @param fromTime
	 * @param toTime
	 * @param ownerRoleId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getMonitorInfoMonthList(String facilityId, Boolean collectorFlg, Long fromTime, Long toTime, String ownerRoleId) throws HinemosDbTimeout {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getMonitorInfoMonthList(facilityId, collectorFlg, fromTime, toTime, ownerRoleId);
	}
	
	/**
	 * MonitorInfoの一覧(RAW)をmonitorTypeIdを条件に取得します。<BR>
	 * 
	 * @param facilityId
	 * @param monitorId
	 * @param fromTime
	 * @param toTime
	 * @param ownerRoleId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getMonitorInfoDataList(String facilityId, String monitorId, Long fromTime, Long toTime, String ownerRoleId) throws HinemosDbTimeout {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getMonitorInfoDataList(facilityId, monitorId,
				fromTime, toTime, ownerRoleId);
	}
	
	/**
	 * MonitorInfoの一覧(RAW)をmonitorTypeIdを条件に取得します。<BR>
	 * 
	 * @param facilityId
	 * @param monitorId
	 * @param fromTime
	 * @param toTime
	 * @param ownerRoleId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getMonitorInfoDataList(String facilityId, Boolean collectorFlg, Long fromTime, Long toTime, String ownerRoleId) throws HinemosDbTimeout {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getMonitorInfoDataList(facilityId, collectorFlg, fromTime, toTime, ownerRoleId);
	}
	
	/**
	 * CollectorItemCodeMstEntityをitemCodeを条件に取得します。<BR>
	 * 
	 * @param itemCode
	 * @return
	 * @throws CollectorNotFound
	 */
	public CollectorItemCodeMstEntity getCollectorItemCodeMst(String itemCode) throws CollectorNotFound {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getCollectorItemCodeMstPK(itemCode, ObjectPrivilegeMode.NONE);
	}
	
	/**
	 * MonitorPriorityTotalの一覧を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param ownerRoleId
	 * @return
	 */
	public List<Object[]> getMonitorPriorityTotalList(String facilityId, Long fromTime, Long toTime, String ownerRoleId) {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getMonitorPriorityTotalList(facilityId, fromTime, toTime, ownerRoleId);
	}
	
	/**
	 * MonitorPriorityDailyの一覧を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param daySec
	 * @param ownerRoleId
	 * @return
	 */
	public List<Object[]> getMonitorPriorityDailyList(String facilityId, Long fromTime, Long toTime, Integer daySec,String ownerRoleId) {
		return PriorityQueryUtil.getMonitorPriorityDailyList(facilityId, fromTime, toTime, daySec, ownerRoleId);
	}
	
	/**
	 * EventLogEntityの一覧を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param ownerRoleId
	 * @return
	 */
	public List<EventLogEntity> getMonitorDetailList(String facilityId, Long fromTime, Long toTime, String ownerRoleId) {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getMonitorDetailList(facilityId, fromTime, toTime, ownerRoleId);
	}
}