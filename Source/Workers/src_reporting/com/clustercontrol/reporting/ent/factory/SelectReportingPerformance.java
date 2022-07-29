/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.ent.factory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.collect.model.CollectKeyInfo;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.QueryExecutor;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.performance.monitor.model.CollectorItemCodeMstEntity;
import com.clustercontrol.reporting.util.PriorityQueryUtil;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.FacilityRelationEntity;

public class SelectReportingPerformance {
	/** ログ出力のインスタンス */
	private static Log m_log = LogFactory.getLog(PriorityQueryUtil.class);
	
	/**
	 * FacilityInfoを取得します。<BR>
	 * 
	 * @param facilityId
	 * @return
	 * @throws FacilityNotFound
	 */
	public FacilityInfo getFacilityInfo(String facilityId) throws FacilityNotFound {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getFacilityPK(facilityId, ObjectPrivilegeMode.NONE);
	}

	/**
	 * 子FacilityRelationEntityを取得します。<BR>
	 * 
	 * @param parentFacilityId
	 * @return
	 */
	public List<FacilityRelationEntity> getChildFacilityRelationEntity(String parentFacilityId) {
		return com.clustercontrol.reporting.util.ReportingQueryUtil.getChildFacilityRelationEntity(parentFacilityId);
	}

	/**
	 * CollectorItemCodeMstEntityの一覧を取得します。<BR>
	 * 
	 * @param itemCodeList
	 * @return
	 */
	public List<CollectorItemCodeMstEntity> getCollectorItemCodeMstListByItemCode(List<String> itemCodeList) {
		return com.clustercontrol.reporting.ent.util.QueryUtil.getCollectorItemCodeMstListByItemCode(itemCodeList);
	}

	/**
	 * MonitorInfoの一覧を取得します。<BR>
	 * 
	 * @param itemCodeList
	 * @return
	 */
	public List<MonitorInfo> getMonitorInfoByItemCode(List<String> itemCodeList) {
		return com.clustercontrol.reporting.ent.util.QueryUtil.getMonitorInfoByItemCode(itemCodeList);
	}

	/**
	 * CollectKeyInfoの一覧を取得します。<BR>
	 * 
	 * @param monitorId
	 * @param facilityidList
	 * @return
	 */
	public List<CollectKeyInfo> getCollectKeyInfoListByMonitorIdAndFacilityidList(String monitorId,
			List<String> facilityidList) {
		return com.clustercontrol.reporting.ent.util.QueryUtil
				.getCollectKeyInfoListByMonitorIdAndFacilityidList(monitorId, facilityidList);
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
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("facilityId", facilityId);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		parameters.put("monitorId", monitorId);
		parameters.put("itemCodeList", itemCodeList);
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			parameters.put("ownerRoleId", ownerRoleId);
			parameters.put("objectType", HinemosModuleConstant.MONITOR);
			parameters.put("objectPrivilege", ObjectPrivilegeMode.READ.name());
		}
		
		return QueryExecutor.getListByJpqlWithTimeout(PriorityQueryUtil.getSummaryPrefAvgDataQuery(ownerRoleId), Object[].class, parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
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
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("facilityId", facilityId);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		parameters.put("monitorId", monitorId);
		parameters.put("itemCodeList", itemCodeList);
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			parameters.put("ownerRoleId", ownerRoleId);
			parameters.put("objectType", HinemosModuleConstant.MONITOR);
			parameters.put("objectPrivilege", ObjectPrivilegeMode.READ.name());
		}
		
		return QueryExecutor.getListByJpqlWithTimeout(PriorityQueryUtil.getSummaryPrefAvgHourQuery(ownerRoleId), Object[].class, parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
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
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("facilityId", facilityId);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		parameters.put("monitorId", monitorId);
		parameters.put("itemCodeList", itemCodeList);
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			parameters.put("ownerRoleId", ownerRoleId);
			parameters.put("objectType", HinemosModuleConstant.MONITOR);
			parameters.put("objectPrivilege", ObjectPrivilegeMode.READ.name());
		}
		
		return QueryExecutor.getListByJpqlWithTimeout(PriorityQueryUtil.getSummaryPrefAvgDayQuery(ownerRoleId), Object[].class, parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
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
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("facilityId", facilityId);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		parameters.put("monitorId", monitorId);
		parameters.put("itemCodeList", itemCodeList);
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			parameters.put("ownerRoleId", ownerRoleId);
			parameters.put("objectType", HinemosModuleConstant.MONITOR);
			parameters.put("objectPrivilege", ObjectPrivilegeMode.READ.name());
		}
		
		return QueryExecutor.getListByJpqlWithTimeout(PriorityQueryUtil.getSummaryPrefAvgMonthQuery(ownerRoleId), Object[].class, parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
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
	 */
	public List<Object[]> getCollectDataList(String facilityId, Long fromTime, Long toTime, String monitorId,
			List<String> itemCodeList, String ownerRoleId) throws HinemosDbTimeout {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("facilityId", facilityId);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		parameters.put("monitorId", monitorId);
		parameters.put("itemCodeList", itemCodeList);
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			parameters.put("ownerRoleId", ownerRoleId);
			parameters.put("objectType", HinemosModuleConstant.MONITOR);
			parameters.put("objectPrivilege", ObjectPrivilegeMode.READ.name());
		}

		return QueryExecutor.getListByJpqlWithTimeout(PriorityQueryUtil.getCollectDataListQuery(ownerRoleId), Object[].class, parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
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
	 */
	public List<Object[]> getSummaryHourList(String facilityId, Long fromTime, Long toTime, String monitorId,
			List<String> itemCodeList, String ownerRoleId) throws HinemosDbTimeout {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("facilityId", facilityId);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		parameters.put("monitorId", monitorId);
		parameters.put("itemCodeList", itemCodeList);
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			parameters.put("ownerRoleId", ownerRoleId);
			parameters.put("objectType", HinemosModuleConstant.MONITOR);
			parameters.put("objectPrivilege", ObjectPrivilegeMode.READ.name());
		}

		return QueryExecutor.getListByJpqlWithTimeout(PriorityQueryUtil.getSummaryHourListQuery(ownerRoleId), Object[].class, parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
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
	 */
	public List<Object[]> getSummaryDayList(String facilityId, Long fromTime, Long toTime, String monitorId,
			List<String> itemCodeList, String ownerRoleId) throws HinemosDbTimeout {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("facilityId", facilityId);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		parameters.put("monitorId", monitorId);
		parameters.put("itemCodeList", itemCodeList);
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			parameters.put("ownerRoleId", ownerRoleId);
			parameters.put("objectType", HinemosModuleConstant.MONITOR);
			parameters.put("objectPrivilege", ObjectPrivilegeMode.READ.name());
		}

		return QueryExecutor.getListByJpqlWithTimeout(PriorityQueryUtil.getSummaryDayListQuery(ownerRoleId), Object[].class, parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
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
	 */
	public List<Object[]> getSummaryMonthList(String facilityId, Long fromTime, Long toTime, String monitorId,
			List<String> itemCodeList, String ownerRoleId) throws HinemosDbTimeout {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("facilityId", facilityId);
		parameters.put("fromTime", fromTime);
		parameters.put("toTime", toTime);
		parameters.put("monitorId", monitorId);
		parameters.put("itemCodeList", itemCodeList);
		// ownerRoleId
		if (ownerRoleId != null && !"".equals(ownerRoleId)) {
			parameters.put("ownerRoleId", ownerRoleId);
			parameters.put("objectType", HinemosModuleConstant.MONITOR);
			parameters.put("objectPrivilege", ObjectPrivilegeMode.READ.name());
		}
		
		return QueryExecutor.getListByJpqlWithTimeout(PriorityQueryUtil.getSummaryMonthListQuery(ownerRoleId), Object[].class, parameters, 
				HinemosPropertyCommon.collect_graph_timeout_reporting.getIntegerValue());
	}
}
