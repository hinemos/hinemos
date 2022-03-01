/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.reporting.factory;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.collect.model.CollectData;
import com.clustercontrol.collect.model.SummaryDay;
import com.clustercontrol.collect.model.SummaryHour;
import com.clustercontrol.collect.model.SummaryMonth;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.reporting.util.ReportingQueryUtil;

public class SelectReportingCollectData {
	
	private static Log m_log = LogFactory.getLog( SelectReportingCollectData.class );
	
	/**
	 * IDと時間を元に収集データを取得します。
	 * 
	 * @return 収集データのリスト
	 * 
	 */
	public List<CollectData> getCollectDataList(List<Integer> idList, Long fromTime, Long toTime)
			throws HinemosDbTimeout, ObjectPrivilege_InvalidRole {
		List<Integer> collectidList = new ArrayList<>();
		for (Integer id : idList) {
			List<String> facilityidList = ReportingQueryUtil.getFacilityId(id); //collectoridに紐づいているファシリティID
			//上記facilityidListの長さは1になる予定
			if(facilityidList.size() != 1){
				m_log.warn("getCollectDataList() facilityidList.size()= " + facilityidList.size());
				continue;
			}

			m_log.debug("collectoId:" + id);
			collectidList.add(id);
		}
		if (collectidList.size() == 0) {
			m_log.debug("collectoIdList.size() :" + collectidList.size());
			return new ArrayList<CollectData>();
		}
		return ReportingQueryUtil.getCollectDataList(collectidList, fromTime, toTime);
	}
	
	/**
	 * IDと時間とオーナーロールIDを元に収集データを取得します。
	 * 
	 * @return 収集データのリスト
	 * 
	 */
	public List<CollectData> getCollectDataList(List<Integer> idList, Long fromTime, Long toTime, String ownerRoleId)
			throws HinemosDbTimeout, ObjectPrivilege_InvalidRole {
		List<Integer> collectidList = new ArrayList<>();
		for (Integer id : idList) {
			List<String> facilityidList = ReportingQueryUtil.getFacilityId(id); //collectoridに紐づいているファシリティID
			//上記facilityidListの長さは1になる予定
			if(facilityidList.size() != 1){
				m_log.warn("getCollectDataList() facilityidList.size()= " + facilityidList.size());
				continue;
			}

			m_log.debug("getCollectDataList() collectoId= " + id);
			collectidList.add(id);
		}
		if (collectidList.size() == 0) {
			m_log.debug("getCollectDataList() collectoIdList.size()= " + collectidList.size());
			return new ArrayList<CollectData>();
		}
		return ReportingQueryUtil.getCollectDataList(collectidList, fromTime, toTime, ownerRoleId);
	}
	
	/**
	 * IDを元に収集データを取得します。
	 * 
	 * @return 収集データのリスト
	 * 
	 */
	public List<CollectData> getCollectDataList(Integer id) {
		return ReportingQueryUtil.getCollectDataList(id);
	}
	
	/**
	 * facilityIdとmonitorIdとdisplayNameとitemCodeと時間を元にサマリデータを取得します。<BR>
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
		return ReportingQueryUtil.getCollectDataList(facilityId, fromTime, toTime, monitorId, displayName, itemCode,
				ownerRoleId);
	}
	
	
	/**
	 * IDと時間を元にサマリデータ(時間単位)を取得します。
	 * 
	 * @return サマリデータ(時間単位)のリスト
	 * 
	 */
	public List<SummaryHour> getSummaryHourList(List<Integer> idList, Long fromTime, Long toTime)
			throws HinemosDbTimeout, ObjectPrivilege_InvalidRole {
		List<Integer> collectIdList = new ArrayList<>();
		for (Integer id : idList) {
			List<String> facilityidList = ReportingQueryUtil.getFacilityId(id);//collectoridに紐づいているファシリティID
			//上記facilityidListの長さは1になる予定
			if(facilityidList.size() != 1){
				m_log.warn("getCollectDataList() facilityidList.size()= " + facilityidList.size());
				continue;
			}
			m_log.debug("collectoId:" + id);
			collectIdList.add(id);
		}
		if (collectIdList.size() == 0) {
			m_log.debug("collectoIdList.size() :" + collectIdList.size());
			return new ArrayList<SummaryHour>();
		}
		return ReportingQueryUtil.getSummaryHourList(collectIdList, fromTime, toTime);
	}
	
	/**
	 * IDと時間とオーナーロールIDを元にサマリデータ(時間単位)を取得します。
	 * 
	 * @return サマリデータ(時間単位)のリスト
	 * 
	 */
	public List<SummaryHour> getSummaryHourList(List<Integer> idList, Long fromTime, Long toTime, String ownerRoleId)
			throws HinemosDbTimeout, ObjectPrivilege_InvalidRole {
		List<Integer> collectIdList = new ArrayList<>();
		for (Integer id : idList) {
			List<String> facilityidList = ReportingQueryUtil.getFacilityId(id);//collectoridに紐づいているファシリティID
			//上記facilityidListの長さは1になる予定
			if(facilityidList.size() != 1){
				m_log.warn("getCollectDataList() facilityidList.size()= " + facilityidList.size());
				continue;
			}
			m_log.debug("getSummaryHourList() collectoId= " + id);
			collectIdList.add(id);
		}
		if (collectIdList.size() == 0) {
			m_log.debug("getSummaryHourList() collectoIdList.size()= " + collectIdList.size());
			return new ArrayList<SummaryHour>();
		}
		return ReportingQueryUtil.getSummaryHourList(collectIdList, fromTime, toTime, ownerRoleId);
	}
	
	/**
	 * IDを元にサマリデータ(時間単位)を取得します。
	 * 
	 * @return サマリデータ(時間単位)のリスト
	 * 
	 */
	public List<SummaryHour> getSummaryHourList(Integer id)throws ObjectPrivilege_InvalidRole{
		List<String> facilityidList = ReportingQueryUtil.getFacilityId(id);//collectoridに紐づいているファシリティID
		//上記facilityidListの長さは1になる予定
		if(facilityidList.size() != 1){
			m_log.warn("getCollectDataList() facilityidList.size()= " + facilityidList.size());
			return null;
		}
		return ReportingQueryUtil.getSummaryHourList(id);
	}
	
	/**
	 * facilityIdとmonitorIdとdisplayNameとitemCodeと時間を元にサマリデータ(時間単位)を取得します。<BR>
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
		return ReportingQueryUtil.getSummaryHourList(facilityId, fromTime, toTime, monitorId, displayName, itemCode,
				ownerRoleId);
	}
	
	/**
	 * IDと時間を元にサマリデータ(日単位)を取得します。
	 * 
	 * @return サマリデータ(日単位)のリスト
	 * 
	 */
	public List<SummaryDay> getSummaryDayList(List<Integer> idList, Long fromTime, Long toTime)
			throws HinemosDbTimeout, ObjectPrivilege_InvalidRole {
		List<Integer> collectIdList = new ArrayList<>();
		for (Integer id : idList) {
			List<String> facilityidList = ReportingQueryUtil.getFacilityId(id); //collectoridに紐づいているファシリティID
			//上記facilityidListの長さは1になる予定
			if(facilityidList.size() != 1){
				m_log.warn("getCollectDataList() facilityidList.size()= " + facilityidList.size());
				continue;
			}
			m_log.debug("collectoId:" + id);
			collectIdList.add(id);
		}
		if (collectIdList.size() == 0) {
			m_log.debug("collectoIdList.size() :" + collectIdList.size());
			return new ArrayList<SummaryDay>();
		}
		return ReportingQueryUtil.getSummaryDayList(collectIdList, fromTime, toTime);
	}
	
	/**
	 * IDと時間とオーナーロールIDを元にサマリデータ(日単位)を取得します。
	 * 
	 * @return サマリデータ(日単位)のリスト
	 * 
	 */
	public List<SummaryDay> getSummaryDayList(List<Integer> idList, Long fromTime, Long toTime, String ownerRoleId)
			throws HinemosDbTimeout, ObjectPrivilege_InvalidRole {
		List<Integer> collectIdList = new ArrayList<>();
		for (Integer id : idList) {
			List<String> facilityidList = ReportingQueryUtil.getFacilityId(id); //collectoridに紐づいているファシリティID
			//上記facilityidListの長さは1になる予定
			if(facilityidList.size() != 1){
				m_log.warn("getCollectDataList() facilityidList.size()= " + facilityidList.size());
				continue;
			}
			m_log.debug("getSummaryDayList() collectoId= " + id);
			collectIdList.add(id);
		}
		if (collectIdList.size() == 0) {
			m_log.debug("getSummaryDayList() collectoIdList.size()= " + collectIdList.size());
			return new ArrayList<SummaryDay>();
		}
		return ReportingQueryUtil.getSummaryDayList(collectIdList, fromTime, toTime, ownerRoleId);
	}
	
	/**
	 * IDを元にサマリデータ(日単位)を取得します。
	 * 
	 * @return サマリデータ(日単位)のリスト
	 * 
	 */
	public List<SummaryDay> getSummaryDayList(Integer id)throws ObjectPrivilege_InvalidRole{
		List<String> facilityidList = ReportingQueryUtil.getFacilityId(id); //collectoridに紐づいているファシリティID
		//上記facilityidListの長さは1になる予定
		if(facilityidList.size() != 1){
			m_log.warn("getCollectDataList() facilityidList.size()=, " + facilityidList.size());
			return null;
		}
		throw new ObjectPrivilege_InvalidRole();
	}
	
	/**
	 * facilityIdとmonitorIdとdisplayNameとitemCodeと時間を元にサマリデータ(日単位)を取得します。<BR>
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
		return ReportingQueryUtil.getSummaryDayList(facilityId, fromTime, toTime, monitorId, displayName, itemCode,
				ownerRoleId);
	}

	/**
	 * IDと時間を元にサマリデータ(月単位)を取得します。
	 * 
	 * @return サマリデータ(月単位)のリスト
	 * 
	 */
	public List<SummaryMonth> getSummaryMonthList(List<Integer> idList, Long fromTime, Long toTime)
			throws HinemosDbTimeout, ObjectPrivilege_InvalidRole {
		List<Integer> collectIdList = new ArrayList<>();
		for (Integer id : idList) {
			List<String> facilityidList = ReportingQueryUtil.getFacilityId(id); //collectoridに紐づいているファシリティID
			//上記facilityidListの長さは1になる予定
			if(facilityidList.size() != 1){
				m_log.warn("getCollectDataList() facilityidList.size()= " + facilityidList.size());
				continue;
			}
			m_log.debug("collectoId:" + id);
			collectIdList.add(id);
		}
		if (collectIdList.size() == 0) {
			m_log.debug("collectoIdList.size() :" + collectIdList.size());
			return new ArrayList<SummaryMonth>();
		}
		return ReportingQueryUtil.getSummaryMonthList(collectIdList, fromTime, toTime);
	}
	
	/**
	 * IDと時間を元にサマリデータ(月単位)を取得します。
	 * 
	 * @return サマリデータ(月単位)のリスト
	 * 
	 */
	public List<SummaryMonth> getSummaryMonthList(List<Integer> idList, Long fromTime, Long toTime, String ownerRoleId)
			throws HinemosDbTimeout, ObjectPrivilege_InvalidRole {
		List<Integer> collectIdList = new ArrayList<>();
		for (Integer id : idList) {
			List<String> facilityidList = ReportingQueryUtil.getFacilityId(id); //collectoridに紐づいているファシリティID
			//上記facilityidListの長さは1になる予定
			if(facilityidList.size() != 1){
				m_log.warn("getCollectDataList() facilityidList.size()= " + facilityidList.size());
				continue;
			}
			m_log.debug("getSummaryMonthList() collectoId= " + id);
			collectIdList.add(id);
		}
		if (collectIdList.size() == 0) {
			m_log.debug("getSummaryMonthList() collectoIdList.size()= " + collectIdList.size());
			return new ArrayList<SummaryMonth>();
		}
		return ReportingQueryUtil.getSummaryMonthList(collectIdList, fromTime, toTime, ownerRoleId);
	}
	
	/**
	 * IDを元にサマリデータ(月単位)を取得します。
	 * 
	 * @return サマリデータ(月単位)のリスト
	 * 
	 */
	public List<SummaryMonth> getSummaryMonthList(Integer id)throws ObjectPrivilege_InvalidRole{
		List<String> facilityidList = ReportingQueryUtil.getFacilityId(id); //collectoridに紐づいているファシリティID
		//上記facilityidListの長さは1になる予定
		if(facilityidList.size() != 1){
			m_log.warn("getCollectDataList() facilityidList.size()= " + facilityidList.size());
			return null;
		}
		return ReportingQueryUtil.getSummaryMonthList(id);
	}
	
	/**
	 * facilityIdとmonitorIdとdisplayNameとitemCodeと時間を元にサマリデータ(月単位)を取得します。<BR>
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
		return ReportingQueryUtil.getSummaryMonthList(facilityId, fromTime, toTime, monitorId, displayName, itemCode,
				ownerRoleId);
	}

	/**
	 * monitorIdを元にitemCodeを取得します。<BR>
	 * 
	 * @param monitorId
	 * @return
	 * @throws HinemosDbTimeout 
	 */
	public List<Object> getCollectItemCodes(String monitorId) throws HinemosDbTimeout {
		return ReportingQueryUtil.getCollectItemCodes(monitorId);
	}
	
}
