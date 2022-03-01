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

import com.clustercontrol.collect.model.CollectKeyInfo;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.performance.monitor.model.CollectorItemCodeMstEntity;
import com.clustercontrol.reporting.ent.factory.SelectReportingPerformance;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.FacilityRelationEntity;


/**
*
* <!-- begin-user-doc --> Enterprise用の性能情報の取得を行うsession bean <!-- end-user-doc --> *
*
*/
public class ReportingPerformanceControllerBean {

	private static Log m_log = LogFactory.getLog( ReportingPerformanceControllerBean.class );
	
	/**
	 * FacilityInfoを取得します<BR>
	 * 
	 * @param facilityId
	 * @return
	 * @throws FacilityNotFound
	 */
	public FacilityInfo getFacilityInfo(String facilityId) throws FacilityNotFound {
		FacilityInfo entity = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingPerformance select = new SelectReportingPerformance();
			entity = select.getFacilityInfo(facilityId);
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
	 * 子FacilityRelationEntityを取得します。<BR>
	 * 
	 * @param parentFacilityId
	 * @return
	 */
	public List<FacilityRelationEntity> getChildFacilityRelationEntity(String parentFacilityId) {
		List<FacilityRelationEntity> list = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingPerformance select = new SelectReportingPerformance();
			list = select.getChildFacilityRelationEntity(parentFacilityId);
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
	 * CollectorItemCodeMstEntityの一覧を取得します。<BR>
	 * 
	 * @param itemCodeList
	 * @return
	 */
	public List<CollectorItemCodeMstEntity> getCollectorItemCodeMstListByItemCode(List<String> itemCodeList) {
		List<CollectorItemCodeMstEntity> list = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingPerformance select = new SelectReportingPerformance();
			list = select.getCollectorItemCodeMstListByItemCode(itemCodeList);
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
	 * MonitorInfoの一覧を取得します。<BR>
	 * 
	 * @param itemCodeList
	 * @return
	 */
	public List<MonitorInfo> getMonitorInfoByItemCode(List<String> itemCodeList) {
		List<MonitorInfo> list = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingPerformance select = new SelectReportingPerformance();
			list = select.getMonitorInfoByItemCode(itemCodeList);
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
	 * CollectKeyInfoの一覧を取得します。<BR>
	 * 
	 * @param monitorId
	 * @param facilityidList
	 * @return
	 */
	public List<CollectKeyInfo> getCollectKeyInfoListByMonitorIdAndFacilityidList(String monitorId, List<String> facilityidList) {
		List<CollectKeyInfo> list = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingPerformance select = new SelectReportingPerformance();
			list = select.getCollectKeyInfoListByMonitorIdAndFacilityidList(monitorId, facilityidList);
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
	 * @param monitorId
	 * @param itemCodeList
	 * @param ownerRoleId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getSummaryPrefAvgData(String facilityId, Long fromTime, Long toTime, String monitorId,
			List<String> itemCodeList, String ownerRoleId) throws HinemosDbTimeout {
		List<Object[]> list = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingPerformance select = new SelectReportingPerformance();
			list = select.getSummaryPrefAvgData(facilityId, fromTime, toTime, monitorId, itemCodeList, ownerRoleId);
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
	 * 時間別のAvgの合計値一覧を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param monitorId
	 * @param itemCodeList
	 * @param ownerRoleId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getSummaryPrefAvgHour(String facilityId, Long fromTime, Long toTime, String monitorId,
			List<String> itemCodeList, String ownerRoleId) throws HinemosDbTimeout {
		List<Object[]> list = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingPerformance select = new SelectReportingPerformance();
			list = select.getSummaryPrefAvgHour(facilityId, fromTime, toTime, monitorId, itemCodeList, ownerRoleId);
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
	 * @param monitorId
	 * @param itemCodeList
	 * @param ownerRoleId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getSummaryPrefAvgDay(String facilityId, Long fromTime, Long toTime, String monitorId,
			List<String> itemCodeList, String ownerRoleId) throws HinemosDbTimeout {
		List<Object[]> list = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingPerformance select = new SelectReportingPerformance();
			list = select.getSummaryPrefAvgDay(facilityId, fromTime, toTime, monitorId, itemCodeList, ownerRoleId);
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
	 * @param monitorId
	 * @param itemCodeList
	 * @param ownerRoleId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getSummaryPrefAvgMonth(String facilityId, Long fromTime, Long toTime, String monitorId,
			List<String> itemCodeList, String ownerRoleId) throws HinemosDbTimeout {
		List<Object[]> list = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingPerformance select = new SelectReportingPerformance();
			list = select.getSummaryPrefAvgMonth(facilityId, fromTime, toTime, monitorId, itemCodeList, ownerRoleId);
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
	 * facilityIdとmonitorIdとitemCode一覧と時間を元にサマリデータを取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param monitorId
	 * @param itemCodeList
	 * @param ownerRoleId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getCollectDataList(String facilityId, Long fromTime, Long toTime, String monitorId,
			List<String> itemCodeList, String ownerRoleId) throws HinemosDbTimeout {
		List<Object[]> list = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingPerformance select = new SelectReportingPerformance();
			list = select.getCollectDataList(facilityId, fromTime, toTime, monitorId, itemCodeList, ownerRoleId);
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
	 * facilityIdとmonitorIdとitemCode一覧と時間を元にサマリデータ(時単位)を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param monitorId
	 * @param itemCodeList
	 * @param ownerRoleId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getSummaryHourList(String facilityId, Long fromTime, Long toTime, String monitorId,
			List<String> itemCodeList, String ownerRoleId) throws HinemosDbTimeout {
		List<Object[]> list = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingPerformance select = new SelectReportingPerformance();
			list = select.getSummaryHourList(facilityId, fromTime, toTime, monitorId, itemCodeList, ownerRoleId);
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
	 * facilityIdとmonitorIdとitemCode一覧と時間を元にサマリデータ(日単位)を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param monitorId
	 * @param itemCodeList
	 * @param ownerRoleId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getSummaryDayList(String facilityId, Long fromTime, Long toTime, String monitorId,
			List<String> itemCodeList, String ownerRoleId) throws HinemosDbTimeout {
		List<Object[]> list = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingPerformance select = new SelectReportingPerformance();
			list = select.getSummaryDayList(facilityId, fromTime, toTime, monitorId, itemCodeList, ownerRoleId);
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
	 * facilityIdとmonitorIdとitemCode一覧と時間を元にサマリデータ(月単位)を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param monitorId
	 * @param itemCodeList
	 * @param ownerRoleId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getSummaryMonthList(String facilityId, Long fromTime, Long toTime, String monitorId,
			List<String> itemCodeList, String ownerRoleId) throws HinemosDbTimeout {
		List<Object[]> list = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingPerformance select = new SelectReportingPerformance();
			list = select.getSummaryMonthList(facilityId, fromTime, toTime, monitorId, itemCodeList, ownerRoleId);
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
