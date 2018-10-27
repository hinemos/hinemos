/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.ent.session;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.jmx.model.JmxCheckInfo;
import com.clustercontrol.jmx.model.JmxMasterInfo;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.reporting.ent.factory.SelectReportingJmx;


/**
*
* <!-- begin-user-doc --> Enterprise用のJMX情報の取得を行うsession bean <!-- end-user-doc --> *
*
*/
public class ReportingJmxControllerBean {

	private static Log m_log = LogFactory.getLog( ReportingJmxControllerBean.class );
	
	/**
	 * JmxCheckInfoを取得します。<BR>
	 * 
	 * @param monitorId
	 * @return
	 * @throws MonitorNotFound
	 */
	public JmxCheckInfo getMonitorJmxInfoPK(String monitorId) throws MonitorNotFound {
		JmxCheckInfo entity = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingJmx select = new SelectReportingJmx();
			entity = select.getMonitorJmxInfoPK(monitorId);
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
		return entity;
	}
	
	/**
	 * JmxCheckInfoを取得します。<BR>
	 * 
	 * @param facilityId
	 * @return
	 * @throws FacilityNotFound
	 */
	public JmxCheckInfo getMonitorJmxInfo(String monitorId, String masterId) throws MonitorNotFound {
		JmxCheckInfo entity = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingJmx select = new SelectReportingJmx();
			entity = select.getMonitorJmxInfo(monitorId, masterId);
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
		return entity;
	}
	
	/**
	 * JmxMasterInfoを取得します。<BR>
	 * 
	 * @param masterId
	 * @return
	 * @throws MonitorNotFound
	 */
	public JmxMasterInfo getJmxMasterInfoPK(String masterId) throws MonitorNotFound {
		JmxMasterInfo entity = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingJmx select = new SelectReportingJmx();
			entity = select.getJmxMasterInfoPK(masterId);
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
		return entity;
	}
	
	/**
	 * MonitorInfoの一覧を取得します。<BR>
	 * 
	 * @param monitorTypeId
	 * @param ownerRoleId
	 * @return
	 */
	public List<MonitorInfo> getMonitorInfoByMonitorTypeId(String monitorTypeId, String ownerRoleId) {
		List<MonitorInfo> list = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingJmx select = new SelectReportingJmx();
			list = select.getMonitorInfoByMonitorTypeId(monitorTypeId, ownerRoleId);
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
	 * MonitorInfoを全一覧を取得します。<BR>
	 * 
	 * @return
	 * @throws HinemosUnknown
	 */
	public List<MonitorInfo> getAllMonitorInfo() throws HinemosUnknown {
		List<MonitorInfo> list = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingJmx select = new SelectReportingJmx();
			list = select.getAllMonitorInfo();
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
		JpaTransactionManager jtm = null;
		List<Object[]> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingJmx select = new SelectReportingJmx();
			list = select.getSummarySumAvgData(facilityId, fromTime, toTime, masterId);
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
		JpaTransactionManager jtm = null;
		List<Object[]> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingJmx select = new SelectReportingJmx();
			list = select.getSummarySumAvgHour(facilityId, fromTime, toTime, masterId);
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
		JpaTransactionManager jtm = null;
		List<Object[]> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingJmx select = new SelectReportingJmx();
			list = select.getSummarySumAvgDay(facilityId, fromTime, toTime, masterId);
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
		JpaTransactionManager jtm = null;
		List<Object[]> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingJmx select = new SelectReportingJmx();
			list = select.getSummaryJmxAvgMonth(facilityId, fromTime, toTime, masterId);
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
}
