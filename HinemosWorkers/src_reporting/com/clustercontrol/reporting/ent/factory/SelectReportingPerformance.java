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
import com.clustercontrol.collect.model.CollectKeyInfo;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.performance.monitor.model.CollectorItemCodeMstEntity;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.model.FacilityRelationEntity;

public class SelectReportingPerformance {

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
	public List<CollectKeyInfo> getCollectKeyInfoListByMonitorIdAndFacilityidList(String monitorId, List<String> facilityidList) {
		return com.clustercontrol.reporting.ent.util.QueryUtil.getCollectKeyInfoListByMonitorIdAndFacilityidList(monitorId, facilityidList);
	}
	
	/**
	 * CollectDataのAvgの合計値一覧を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param monitorId
	 * @param itemCodeList
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getSummaryPrefAvgData(String facilityId, Long fromTime, Long toTime, String monitorId,
			List<String> itemCodeList) throws HinemosDbTimeout {
		return com.clustercontrol.reporting.ent.util.QueryUtil.getSummaryPrefAvgData(facilityId, fromTime, toTime, monitorId, itemCodeList);
	}
	
	/**
	 * 時間別のAvgの合計値一覧を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param monitorId
	 * @param itemCodeList
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getSummaryPrefAvgHour(String facilityId, Long fromTime, Long toTime, String monitorId,
			List<String> itemCodeList) throws HinemosDbTimeout {
		return com.clustercontrol.reporting.ent.util.QueryUtil.getSummaryPrefAvgHour(facilityId, fromTime, toTime, monitorId, itemCodeList);
	}
	
	/**
	 * 日別のAvgの合計値一覧を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param monitorId
	 * @param itemCodeList
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getSummaryPrefAvgDay(String facilityId, Long fromTime, Long toTime, String monitorId,
			List<String> itemCodeList) throws HinemosDbTimeout {
		return com.clustercontrol.reporting.ent.util.QueryUtil.getSummaryPrefAvgDay(facilityId, fromTime, toTime, monitorId, itemCodeList);
	}
	
	/**
	 * 月別のAvgの合計値一覧を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param monitorId
	 * @param itemCodeList
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object[]> getSummaryPrefAvgMonth(String facilityId, Long fromTime, Long toTime, String monitorId,
			List<String> itemCodeList) throws HinemosDbTimeout {
		return com.clustercontrol.reporting.ent.util.QueryUtil.getSummaryPrefAvgMonth(facilityId, fromTime, toTime, monitorId, itemCodeList);
	}
	
	/**
	 * facilityIdとmonitorIdとitemCode一覧と時間を元にサマリデータを取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param monitorId
	 * @param itemCodeList
	 * @return
	 */
	public List<Object[]> getCollectDataList(String facilityId, Long fromTime, Long toTime, String monitorId, List<String> itemCodeList) 
		throws HinemosDbTimeout {
		return com.clustercontrol.reporting.ent.util.QueryUtil.getCollectDataList(facilityId, fromTime, toTime, monitorId, itemCodeList);
	}
	
	/**
	 * facilityIdとmonitorIdとitemCode一覧と時間を元にサマリデータ(時単位)を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param monitorId
	 * @param itemCodeList
	 * @return
	 */
	public List<Object[]> getSummaryHourList(String facilityId, Long fromTime, Long toTime, String monitorId, List<String> itemCodeList) 
			throws HinemosDbTimeout {
		return com.clustercontrol.reporting.ent.util.QueryUtil.getSummaryHourList(facilityId, fromTime, toTime, monitorId, itemCodeList);
	}
	
	/**
	 * facilityIdとmonitorIdとitemCode一覧と時間を元にサマリデータ(日単位)を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param monitorId
	 * @param itemCodeList
	 * @return
	 */
	public List<Object[]> getSummaryDayList(String facilityId, Long fromTime, Long toTime, String monitorId, List<String> itemCodeList) 
			throws HinemosDbTimeout {
		return com.clustercontrol.reporting.ent.util.QueryUtil.getSummaryDayList(facilityId, fromTime, toTime, monitorId, itemCodeList);
	}
	
	/**
	 * facilityIdとmonitorIdとitemCode一覧と時間を元にサマリデータ(月単位)を取得します。<BR>
	 * 
	 * @param facilityId
	 * @param fromTime
	 * @param toTime
	 * @param monitorId
	 * @param itemCodeList
	 * @return
	 */
	public List<Object[]> getSummaryMonthList(String facilityId, Long fromTime, Long toTime, String monitorId, List<String> itemCodeList) 
			throws HinemosDbTimeout {
		return com.clustercontrol.reporting.ent.util.QueryUtil.getSummaryMonthList(facilityId, fromTime, toTime, monitorId, itemCodeList);
	}
}
