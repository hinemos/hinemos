/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.session;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.collect.bean.SummaryTypeConstant;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.CollectorNotFound;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.notify.monitor.model.EventLogEntity;
import com.clustercontrol.performance.monitor.model.CollectorItemCodeMstEntity;
import com.clustercontrol.reporting.ReportUtil;
import com.clustercontrol.reporting.factory.SelectReportingMonitor;


/**
*
* <!-- begin-user-doc --> 監視情報の制御を行うsession bean <!-- end-user-doc --> *
*
*/
public class ReportingMonitorControllerBean {

	private static Log m_log = LogFactory.getLog(ReportingMonitorControllerBean.class);
	
	/**
	 * MonitorInfoを取得します。<BR>
	 * 
	 * @param monitorId
	 * @return
	 * @throws MonitorNotFound
	 */
	public MonitorInfo getMonitorInfo(String monitorId) throws MonitorNotFound {
		SelectReportingMonitor select = new SelectReportingMonitor();
		MonitorInfo entity = select.getMonitorInfo(monitorId);
		return entity;
	}
	
	/**
	 * MonitorInfoの一覧を取得します。<BR>
	 * 
	 * @param itemFilter
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param ownerRoleId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getMonitorList(String itemFilter, String facilityId, Long fromTime, Long toTime, String ownerRoleId) throws HinemosDbTimeout {
		SelectReportingMonitor select = new SelectReportingMonitor();
		List<Object[]> list = null;
		JpaTransactionManager jtm = null;
		int summaryType = ReportUtil.getSummaryType(fromTime, toTime);
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			
			// サマリデータ、または収集データ(raw)のタイプでスイッチ
			switch (summaryType) {
			case SummaryTypeConstant.TYPE_AVG_HOUR:
				list = select.getMonitorHourList(itemFilter, facilityId, fromTime, toTime, ownerRoleId);
				break;
			case SummaryTypeConstant.TYPE_AVG_DAY:
				list = select.getMonitorDayList(itemFilter, facilityId, fromTime, toTime, ownerRoleId);
				break;
			case SummaryTypeConstant.TYPE_AVG_MONTH:
				list = select.getMonitorMonthList(itemFilter, facilityId, fromTime, toTime, ownerRoleId);
				break;
			default: // defaultはRAWとする
				list = select.getMonitorDataList(itemFilter, facilityId, fromTime, toTime, ownerRoleId);
				break;
			}
			
			jtm.commit();
		} catch (HinemosDbTimeout e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.error("error", e);
			if (jtm != null) {
				jtm.rollback();
			}
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return list;
	}
	
	/**
	 * MonitorInfoの一覧をmonitorTypeIdを条件に取得します。<BR>
	 * 
	 * @param facilityId
	 * @param monitorId
	 * @param fromTime
	 * @param toTime
	 * @param collectorFlg
	 * @param ownerRoleId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getMonitorInfoListByMonitorTypeId(String facilityId, String monitorId, Long fromTime, Long toTime, Boolean collectorFlg, String ownerRoleId) throws HinemosDbTimeout {
		SelectReportingMonitor select = new SelectReportingMonitor();
		JpaTransactionManager jtm = null;
		List<Object[]> list = null;
		int summaryType = ReportUtil.getSummaryType(fromTime, toTime);
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			// サマリデータ、または収集データ(raw)のタイプでスイッチ
			switch (summaryType) {
			case SummaryTypeConstant.TYPE_AVG_HOUR:
				if (monitorId != null && collectorFlg == null) {
					list = select.getMonitorInfoHourList(facilityId, monitorId, fromTime, toTime, ownerRoleId);
				} else {
					list = select.getMonitorInfoHourList(facilityId, collectorFlg, fromTime, toTime, ownerRoleId);
				}
				break;
			case SummaryTypeConstant.TYPE_AVG_DAY:
				if (monitorId != null && collectorFlg == null) {
					list = select.getMonitorInfoDayList(facilityId, monitorId, fromTime, toTime, ownerRoleId);
				} else {
					list = select.getMonitorInfoDayList(facilityId, collectorFlg, fromTime, toTime, ownerRoleId);
				}
				break;
			case SummaryTypeConstant.TYPE_AVG_MONTH:
				if (monitorId != null && collectorFlg == null) {
					list = select.getMonitorInfoMonthList(facilityId, monitorId, fromTime, toTime, ownerRoleId);
				} else {
					list = select.getMonitorInfoMonthList(facilityId, collectorFlg, fromTime, toTime, ownerRoleId);
				}
				break;
			default: // defaultはRAWとする
				if (monitorId != null && collectorFlg == null) {
					list = select.getMonitorInfoDataList(facilityId, monitorId, fromTime, toTime, ownerRoleId);
				} else {
					list = select.getMonitorInfoDataList(facilityId, collectorFlg, fromTime, toTime, ownerRoleId);
				}
				break;
			}
			jtm.commit();
		} catch (HinemosDbTimeout e) {
			if (jtm != null) {
				jtm.rollback();
			}
			throw e;
		} catch (Exception e) {
			m_log.error("error", e);
			if (jtm != null) {
				jtm.rollback();
			}
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return list;
	}
	
	/**
	 * ReportingCollectorItemCodeMstEntityを取得します。<BR>
	 *
	 *
	 * @return key
	 * @throws CollectorNotFound 
	 */
	public CollectorItemCodeMstEntity getReportingCollectorItemCodeMst(String itemCode) throws CollectorNotFound {
		SelectReportingMonitor select = new SelectReportingMonitor();
		CollectorItemCodeMstEntity entity = select.getCollectorItemCodeMst(itemCode);
		return entity;
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
		SelectReportingMonitor select = new SelectReportingMonitor();
		List<Object[]> list = select.getMonitorPriorityTotalList(facilityId, fromTime, toTime, ownerRoleId);
		return list;
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
		SelectReportingMonitor select = new SelectReportingMonitor();
		List<Object[]> list = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			list = select.getMonitorPriorityDailyList(facilityId, fromTime, toTime, daySec, ownerRoleId);
			jtm.commit();
		} catch (Exception e) {
			if (jtm != null) {
				jtm.rollback();
			}
		} finally {
			if (jtm != null) {
				jtm.close();
			}
		}
		return list;
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
		SelectReportingMonitor select = new SelectReportingMonitor();
		List<EventLogEntity> list = select.getMonitorDetailList(facilityId, fromTime, toTime, ownerRoleId);
		return list;
	}
}
