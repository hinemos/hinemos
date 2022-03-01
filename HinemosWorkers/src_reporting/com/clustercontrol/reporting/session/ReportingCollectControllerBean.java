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

import com.clustercontrol.collect.model.CollectData;
import com.clustercontrol.collect.model.CollectKeyInfo;
import com.clustercontrol.collect.model.SummaryDay;
import com.clustercontrol.collect.model.SummaryHour;
import com.clustercontrol.collect.model.SummaryMonth;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.reporting.factory.SelectReportingCollectData;
import com.clustercontrol.reporting.factory.SelectReportingCollectKeyInfo;


/**
*
* <!-- begin-user-doc --> 収集情報の制御を行うsession bean <!-- end-user-doc --> *
*
*/
public class ReportingCollectControllerBean{

	private static Log m_log = LogFactory.getLog(ReportingCollectControllerBean.class);
	
	/**
	 * collectoridを取得します。<BR>
	 *
	 *
	 * @return collectorid
	 */
	public Integer getCollectId(String itemName, String displayName, String monitorId, String facilityId) {
		Integer collectorid = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingCollectKeyInfo select = new SelectReportingCollectKeyInfo();
			collectorid = select.getCollectId(itemName, displayName, monitorId, facilityId);
			jtm.commit();
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
		return collectorid;
	}
	
	/**
	 * ReportingCollectKeyInfoを取得します。<BR>
	 *
	 *
	 * @return key
	 */
	public CollectKeyInfo getReportingCollectKeyInfo(String itemName, String displayName, String monitorId, String facilityId) {
		CollectKeyInfo key = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingCollectKeyInfo select = new SelectReportingCollectKeyInfo();
			key = select.getReportingCollectKeyInfo(itemName, displayName, monitorId, facilityId);
			jtm.commit();
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
		return key;
	}
	
	/**
	 * ReportingCollectKeyInfoの一覧をmonitorIdとfacilityIdを条件に取得します。<BR>
	 * 
	 * @param monitorIdList
	 * @param facilityId
	 * @return
	 */
	public List<CollectKeyInfo> getReportCollectKeyList(String monitorId, String facilityId) {
		JpaTransactionManager jtm = null;
		List<CollectKeyInfo> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingCollectKeyInfo select = new SelectReportingCollectKeyInfo();
			list = select.getReportCollectKeyList(monitorId, facilityId);
			jtm.commit();
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
	 * ReportingCollectKeyInfoの一覧を取得します。<BR>
	 * 
	 * @param itemname
	 * @param displayName
	 * @param monitorid
	 * @param facilityid
	 * @return list
	 */
	public List<CollectKeyInfo> getReportingCollectKeyInfoList(String itemname, String displayName, String monitorid, String facilityid) {
		List<CollectKeyInfo> list = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingCollectKeyInfo select = new SelectReportingCollectKeyInfo();
			list = select.getReportingCollectKeyInfoList(itemname, displayName, monitorid, facilityid);
			jtm.commit();
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
	 * IDと時間を指定し、その時間内の収集データのリストを取得します。<BR>
	 *
	 *
	 * @return CollectDataのリスト
	 * @throws HinemosDbTimeout 
	 * @throws InvalidRole 
	 */
	public List<CollectData> getCollectDataList(List<Integer> idList, Long fromTime, Long toTime) throws HinemosDbTimeout {
		List<CollectData> list = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingCollectData select = new SelectReportingCollectData();
			list = select.getCollectDataList(idList, fromTime, toTime);
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
	 * IDと時間とオーナーロールIDを指定し、その時間内の収集データのリストを取得します。<BR>
	 *
	 *
	 * @return CollectDataのリスト
	 * @throws HinemosDbTimeout 
	 * @throws InvalidRole 
	 */
	public List<CollectData> getCollectDataList(List<Integer> idList, Long fromTime, Long toTime, String ownerRoleId) throws HinemosDbTimeout {
		List<CollectData> list = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingCollectData select = new SelectReportingCollectData();
			list = select.getCollectDataList(idList, fromTime, toTime, ownerRoleId);
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
	 * facilityIdとmonitorIdとdisplayNameとitemCode時間を元にサマリデータを取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param monitorId
	 * @param displayName
	 * @param itemCode
	 * @param ownerRoleId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getCollectDataList(String facilityId, Long fromTime, Long toTime, String monitorId,
			String displayName, String itemCode, String ownerRoleId) throws HinemosDbTimeout {
		JpaTransactionManager jtm = null;
		List<Object[]> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingCollectData select = new SelectReportingCollectData();
			list = select.getCollectDataList(facilityId, fromTime, toTime, monitorId, displayName, itemCode, ownerRoleId);
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
	 * IDを指定し、IDに紐づく収集データのリストを取得します。<BR>
	 *
	 *
	 * @return CollectDataのリスト
	 * @throws InvalidRole 
	 */
	public List<CollectData> getCollectDataList(Integer id) {
		List<CollectData> list = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingCollectData select = new SelectReportingCollectData();
			list = select.getCollectDataList(id);
			jtm.commit();
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
	 * IDと時間を指定し、その時間内のサマリデータ(時)のリストを取得します。<BR>
	 *
	 *
	 * @return SummaryHourのリスト
	 * @throws HinemosDbTimeout 
	 * @throws InvalidRole 
	 */
	public List<SummaryHour> getSummaryHourList(List<Integer> idList, Long fromTime, Long toTime) throws HinemosDbTimeout {
		List<SummaryHour> list = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingCollectData select = new SelectReportingCollectData();
			list = select.getSummaryHourList(idList, fromTime, toTime);
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
	 * IDと時間とオーナーロールIDを指定し、その時間内のサマリデータ(時)のリストを取得します。<BR>
	 *
	 *
	 * @return SummaryHourのリスト
	 * @throws HinemosDbTimeout 
	 * @throws InvalidRole 
	 */
	public List<SummaryHour> getSummaryHourList(List<Integer> idList, Long fromTime, Long toTime,
			String ownerRoleId) throws HinemosDbTimeout {
		List<SummaryHour> list = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingCollectData select = new SelectReportingCollectData();
			list = select.getSummaryHourList(idList, fromTime, toTime, ownerRoleId);
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
	 * facilityIdとmonitorIdとdisplayNameとitemCode時間を元にサマリデータ(時間単位)を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param monitorId
	 * @param displayName
	 * @param itemCode
	 * @param ownerRoleId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getSummaryHourList(String facilityId, Long fromTime, Long toTime, String monitorId,
			String displayName, String itemCode, String ownerRoleId) throws HinemosDbTimeout {
		JpaTransactionManager jtm = null;
		List<Object[]> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingCollectData select = new SelectReportingCollectData();
			list = select.getSummaryHourList(facilityId, fromTime, toTime, monitorId, displayName, itemCode, ownerRoleId);
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
	 * IDを指定し、サマリデータ(時)のリストを取得します。<BR>
	 *
	 *
	 * @return SummaryHourのリスト
	 * @throws InvalidRole 
	 */
	public List<SummaryHour> getSummaryHourList(Integer id) {
		List<SummaryHour> list = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingCollectData select = new SelectReportingCollectData();
			list = select.getSummaryHourList(id);
			jtm.commit();
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
	 * IDと時間を指定し、その時間内のサマリデータ(日)のリストを取得します。<BR>
	 *
	 *
	 * @return SummaryDayのリスト
	 * @throws HinemosDbTimeout 
	 */
	public List<SummaryDay> getSummaryDayList(List<Integer> idList, Long fromTime, Long toTime) throws HinemosDbTimeout {
		List<SummaryDay> list = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingCollectData select = new SelectReportingCollectData();
			list = select.getSummaryDayList(idList, fromTime, toTime);
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
	 * IDと時間とオーナーロールIDを指定し、その時間内のサマリデータ(日)のリストを取得します。<BR>
	 *
	 *
	 * @return SummaryDayのリスト
	 * @throws HinemosDbTimeout 
	 */
	public List<SummaryDay> getSummaryDayList(List<Integer> idList, Long fromTime, Long toTime,
			String ownerRoleId) throws HinemosDbTimeout {
		List<SummaryDay> list = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingCollectData select = new SelectReportingCollectData();
			list = select.getSummaryDayList(idList, fromTime, toTime, ownerRoleId);
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
	 * IDを指定し、その時間内のサマリデータ(日)のリストを取得します。<BR>
	 *
	 *
	 * @return SummaryDayのリスト
	 */
	public List<SummaryDay> getSummaryDayList(Integer id) {
		List<SummaryDay> list = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingCollectData select = new SelectReportingCollectData();
			list = select.getSummaryDayList(id);
			jtm.commit();
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
	 * facilityIdとmonitorIdとdisplayNameとitemCode時間を元にサマリデータ(日単位)を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param monitorId
	 * @param displayName
	 * @param itemCode
	 * @param ownerRoleId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getSummaryDayList(String facilityId, Long fromTime, Long toTime, String monitorId,
			String displayName, String itemCode, String ownerRoleId) throws HinemosDbTimeout {
		List<Object[]> list = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingCollectData select = new SelectReportingCollectData();
			list = select.getSummaryDayList(facilityId, fromTime, toTime, monitorId, displayName, itemCode, ownerRoleId);
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
	 * IDと時間を指定し、その時間内のサマリデータ(月)のリストを取得します。<BR>
	 *
	 *
	 * @return SummaryMonthのリスト
	 * @throws HinemosDbTimeout 
	 * @throws InvalidRole 
	 * @throws HinemosUnknown 
	 */
	public List<SummaryMonth> getSummaryMonthList(List<Integer> idList, Long fromTime, Long toTime) throws HinemosDbTimeout {
		List<SummaryMonth> list = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingCollectData select = new SelectReportingCollectData();
			list = select.getSummaryMonthList(idList, fromTime, toTime);
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
	 * IDと時間とオーナーロールIDを指定し、その時間内のサマリデータ(月)のリストを取得します。<BR>
	 *
	 *
	 * @return SummaryMonthのリスト
	 * @throws HinemosDbTimeout
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public List<SummaryMonth> getSummaryMonthList(List<Integer> idList, Long fromTime, Long toTime, String ownerRoleId)
			throws HinemosDbTimeout {
		List<SummaryMonth> list = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingCollectData select = new SelectReportingCollectData();
			list = select.getSummaryMonthList(idList, fromTime, toTime, ownerRoleId);
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
	 * IDを指定し、サマリデータ(月)のリストを取得します。<BR>
	 *
	 *
	 * @return SummaryMonthのリスト
	 * @throws InvalidRole 
	 * @throws HinemosUnknown 
	 */
	public List<SummaryMonth> getSummaryMonthList(Integer id) {
		List<SummaryMonth> list = null;
		JpaTransactionManager jtm = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingCollectData select = new SelectReportingCollectData();
			list = select.getSummaryMonthList(id);
			jtm.commit();
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
	 * facilityIdとmonitorIdとdisplayNameとitemCode時間を元にサマリデータ(月単位)を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param monitorId
	 * @param displayName
	 * @param itemCode
	 * @param ownerRoleId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getSummaryMonthList(String facilityId, Long fromTime, Long toTime, String monitorId,
			String displayName, String itemCode, String ownerRoleId) throws HinemosDbTimeout {
		JpaTransactionManager jtm = null;
		List<Object[]> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingCollectData select = new SelectReportingCollectData();
			list = select.getSummaryMonthList(facilityId, fromTime, toTime, monitorId, displayName, itemCode, ownerRoleId);
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
	 * monitorIdを元にitemCodeを取得します。<BR>
	 * 
	 * @param monitorId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object> getCollectItemCodes(String monitorId) throws HinemosDbTimeout {
		JpaTransactionManager jtm = null;
		List<Object> list = null;
		try {
			jtm = new JpaTransactionManager();
			jtm.begin();
			SelectReportingCollectData select = new SelectReportingCollectData();
			list = select.getCollectItemCodes(monitorId);
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
}
