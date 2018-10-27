/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.collect.factory;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.RoleSettingTreeConstant;
import com.clustercontrol.collect.bean.CollectConstant;
import com.clustercontrol.collect.model.CollectData;
import com.clustercontrol.collect.model.CollectKeyInfo;
import com.clustercontrol.collect.model.SummaryDay;
import com.clustercontrol.collect.model.SummaryHour;
import com.clustercontrol.collect.model.SummaryMonth;
import com.clustercontrol.collect.util.QueryUtil;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.fault.HinemosDbTimeout;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.JobMasterNotFound;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.monitor.run.bean.MonitorTypeConstant;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.session.MonitorSettingControllerBean;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;
import com.clustercontrol.repository.util.FacilityTreeCache;

public class SelectCollectData {
	
	private static Log m_log = LogFactory.getLog( SelectCollectData.class );

	/**
	 * IDと時間を元に収集データを取得します。
	 * 
	 * @param idList 収集キーのリスト
	 * @param fromTime 開始日時
	 * @param toTime 終了日時
	 * @return 収集データのリスト
	 * @throws ObjectPrivilege_InvalidRole
	 * @throws MonitorNotFound
	 * @throws JobMasterNotFound
	 * @throws HinemosDbTimeout
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public List<CollectData> getCollectDataList(List<Integer> idList, Long fromTime, Long toTime)
			throws ObjectPrivilege_InvalidRole, MonitorNotFound, JobMasterNotFound, 
			HinemosDbTimeout, HinemosUnknown, InvalidRole {
		// 有効な情報を設定する
		List<Integer> collectIdList = getValidCollectIdList(idList);
		if (collectIdList.size() == 0) {
			m_log.debug("collectoIdList.size() :" + collectIdList.size());
			return new ArrayList<CollectData>();
		}
		return QueryUtil.getCollectDataList(collectIdList, fromTime, toTime, 
				HinemosPropertyCommon.collect_graph_timeout.getIntegerValue());
	}

	/**
	 * IDを元に収集データを取得します。
	 * 
	 * @param id 収集キー
	 * @return 収集データのリスト
	 * @throws ObjectPrivilege_InvalidRole
	 * @throws MonitorNotFound
	 * @throws JobMasterNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public List<CollectData> getCollectDataList(Integer id) 
			throws ObjectPrivilege_InvalidRole, MonitorNotFound, JobMasterNotFound, HinemosUnknown, InvalidRole {
		// 収集データが有効か確認する
		if (isValidCollectId(id)) {
			//時間を指定しないので、collectoridに紐づくデータを全て取ってくる
			return QueryUtil.getCollectDataList(id);
		} else {
			return null;
		}
	}
	
	/**
	 * IDと時間を元にサマリデータ(時間単位)を取得します。
	 * 
	 * @param idList 収集キーのリスト
	 * @param fromTime 開始日時
	 * @param toTime 終了日時
	 * @return サマリデータ(時間単位)のリスト
	 * @throws ObjectPrivilege_InvalidRole
	 * @throws MonitorNotFound
	 * @throws JobMasterNotFound
	 * @throws HinemosDbTimeout
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public List<SummaryHour> getSummaryHourList(List<Integer> idList, Long fromTime, Long toTime) 
			throws ObjectPrivilege_InvalidRole, MonitorNotFound, JobMasterNotFound, 
			HinemosDbTimeout, HinemosUnknown, InvalidRole{
		// 有効な情報を設定する
		List<Integer> collectIdList = getValidCollectIdList(idList);
		if (collectIdList.size() == 0) {
			m_log.debug("collectoIdList.size() :" + collectIdList.size());
			return new ArrayList<SummaryHour>();
		}
		return QueryUtil.getSummaryHourList(collectIdList, fromTime, toTime, 
				HinemosPropertyCommon.collect_graph_timeout.getIntegerValue());
	}
	
	/**
	 * IDを元にサマリデータ(時間単位)を取得します。
	 * 
	 * @param id 収集キー
	 * @return サマリデータ(時間単位)のリスト
	 * @throws ObjectPrivilege_InvalidRole
	 * @throws MonitorNotFound
	 * @throws JobMasterNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * 
	 */
	public List<SummaryHour> getSummaryHourList(Integer id) 
			throws ObjectPrivilege_InvalidRole, MonitorNotFound, JobMasterNotFound, HinemosUnknown, InvalidRole{
		// 収集データが有効か確認する
		if (isValidCollectId(id)) {
			//時間を指定しないので、collectoridに紐づくデータを全て取ってくる
			return QueryUtil.getSummaryHourList(id);
		} else {
			return null;
		}
	}
	
	/**
	 * IDと時間を元にサマリデータ(日単位)を取得します。
	 * 
	 * @param idList 収集キーのリスト
	 * @param fromTime 開始日時
	 * @param toTime 終了日時
	 * @return サマリデータ(日単位)のリスト
	 * @throws ObjectPrivilege_InvalidRole
	 * @throws MonitorNotFound
	 * @throws JobMasterNotFound
	 * @throws HinemosDbTimeout
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public List<SummaryDay> getSummaryDayList(List<Integer> idList, Long fromTime, Long toTime) 
			throws ObjectPrivilege_InvalidRole, MonitorNotFound, JobMasterNotFound, 
			HinemosDbTimeout, HinemosUnknown, InvalidRole{
		// 有効な情報を設定する
		List<Integer> collectIdList = getValidCollectIdList(idList);
		if (collectIdList.size() == 0) {
			m_log.debug("collectoIdList.size() :" + collectIdList.size());
			return new ArrayList<SummaryDay>();
		}
		return QueryUtil.getSummaryDayList(collectIdList, fromTime, toTime, 
				HinemosPropertyCommon.collect_graph_timeout.getIntegerValue());
	}
	
	/**
	 * IDを元にサマリデータ(日単位)を取得します。
	 * 
	 * @param id 収集キー
	 * @return サマリデータ(日単位)のリスト
	 * @throws ObjectPrivilege_InvalidRole
	 * @throws MonitorNotFound
	 * @throws JobMasterNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public List<SummaryDay> getSummaryDayList(Integer id) throws ObjectPrivilege_InvalidRole, 
		MonitorNotFound, JobMasterNotFound, HinemosUnknown, InvalidRole {
		// 収集データが有効か確認する
		if (isValidCollectId(id)) {
			//時間を指定しないので、collectoridに紐づくデータを全て取ってくる
			return QueryUtil.getSummaryDayList(id);
		} else {
			return null;
		}
	}
	
	
	/**
	 * IDと時間を元にサマリデータ(月単位)を取得します。
	 * 
	 * @param idList 収集キーのリスト
	 * @param fromTime 開始日時
	 * @param toTime 終了日時
	 * @return サマリデータ(月単位)のリスト
	 * @throws ObjectPrivilege_InvalidRole
	 * @throws MonitorNotFound
	 * @throws JobMasterNotFound
	 * @throws HinemosDbTimeout
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public List<SummaryMonth> getSummaryMonthList(List<Integer> idList, Long fromTime, Long toTime)
			throws ObjectPrivilege_InvalidRole, MonitorNotFound, JobMasterNotFound, 
			HinemosDbTimeout, HinemosUnknown, InvalidRole {
		// 有効な情報を設定する
		List<Integer> collectIdList = getValidCollectIdList(idList);
		if (collectIdList.size() == 0) {
			m_log.debug("collectoIdList.size() :" + collectIdList.size());
			return new ArrayList<SummaryMonth>();
		}
		return QueryUtil.getSummaryMonthList(collectIdList, fromTime, toTime, 
				HinemosPropertyCommon.collect_graph_timeout.getIntegerValue());
	}
	
	/**
	 * IDを元にサマリデータ(月単位)を取得します。
	 * 
	 * @param id 収集キー
	 * @return サマリデータ(月単位)のリスト
	 * @throws ObjectPrivilege_InvalidRole
	 * @throws MonitorNotFound
	 * @throws JobMasterNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public List<SummaryMonth> getSummaryMonthList(Integer id) throws ObjectPrivilege_InvalidRole, 
		MonitorNotFound, JobMasterNotFound, HinemosUnknown, InvalidRole{
		// 収集データが有効か確認する
		if (isValidCollectId(id)) {
			//時間を指定しないので、collectoridに紐づくデータを全て取ってくる
			return QueryUtil.getSummaryMonthList(id);
		} else {
			return null;
		}
	}
	
	/**
	 * 権限が有効な収集キーを返す
	 * 
	 * @param idList　収集キーのリスト
	 * @param fromTime 収集開始日時
	 * @param toTime 収集終了日時
	 * @return 有効な収集キーのリスト
	 * @throws ObjectPrivilege_InvalidRole
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	private List<Integer> getValidCollectIdList(List<Integer> idList)
			throws ObjectPrivilege_InvalidRole, MonitorNotFound, JobMasterNotFound, HinemosUnknown, InvalidRole {
		// FIXME メソッドの引数としてオーナーロールを貰ってくるべきか要確認
		// ログインユーザでオブジェクト権限チェックしつつ収集データを取得
		String userId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
		List<FacilityInfo> facilityUserList = FacilityTreeCache.getNodeFacilityInfoListByUserId(userId); //ログインユーザが見ることができるファシリティID一覧
		List<Integer> collectIdList = new ArrayList<>();
		
		for (Integer id : idList) {
			List<CollectKeyInfo> collectKeyInfoList = QueryUtil.getCollectKeyInfoList(id); //collectoridに紐づいているCollectKeyInfo
			//上記collectKeyInfoListの長さは1になる予定
			if(collectKeyInfoList.size() != 1){
				m_log.warn("getCollectDataList() collectKeyInfoList.size()= " + collectKeyInfoList.size());
				continue;
			}
			// facilityIdが可視範囲にあるかチェック
			String facilityId = collectKeyInfoList.get(0).getFacilityid();
			if (!facilityId.equals(RoleSettingTreeConstant.ROOT_ID)) {
				boolean find = false;
				for (FacilityInfo facilityInfo : facilityUserList) {
					if (facilityInfo.getFacilityId().equals(facilityId)) {
						find = true;
						break;
					}
				}
				if (!find) {
					throw new ObjectPrivilege_InvalidRole();
				}
			}
			if (!collectKeyInfoList.get(0).getMonitorId().equals(CollectConstant.COLLECT_TYPE_JOB)) {
				// monitorIdが可視範囲にあるかチェック
				new MonitorSettingControllerBean().getMonitor(collectKeyInfoList.get(0).getMonitorId());
			} else {
				// ジョブ履歴
				String[] jobIds = collectKeyInfoList.get(0).getDisplayName().split(CollectConstant.COLLECT_TYPE_JOB_DELIMITER);
				if (jobIds.length < 2) {
					throw new InvalidRole("jobunitId:jobId=" + collectKeyInfoList.get(0).getDisplayName() + " does not exist");
				}
				// jobunitIdが可視範囲にあるかチェック
				com.clustercontrol.jobmanagement.util.QueryUtil.getJobMstPK(jobIds[0], jobIds[1]);
			}

			m_log.debug("collectoId:" + id);
			collectIdList.add(id);
		}
		return collectIdList;
	}
	
	/**
	 * 権限が有効かどうかを返す
	 * 
	 * @param id 収集キー
	 * @return true:有効、false：無効
	 * @throws ObjectPrivilege_InvalidRole
	 * @throws MonitorNotFound
	 * @throws JobMasterNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	private boolean isValidCollectId(Integer id) throws ObjectPrivilege_InvalidRole, 
		MonitorNotFound, JobMasterNotFound, HinemosUnknown, InvalidRole{

		// FIXME メソッドの引数としてオーナーロールを貰ってくるべきか要確認
		// ログインユーザでオブジェクト権限チェックしつつ収集データを取得
		String userId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
		List<FacilityInfo> facilityUserList = FacilityTreeCache.getNodeFacilityInfoListByUserId(userId); //ログインユーザが見ることができるファシリティID一覧
		List<CollectKeyInfo> collectKeyInfoList = QueryUtil.getCollectKeyInfoList(id); //collectoridに紐づいているCollectKeyInfo
		//上記facilityidListの長さは1になる予定
		if(collectKeyInfoList.size() != 1){
			m_log.warn("getCollectDataList() collectKeyInfoList.size()= " + collectKeyInfoList.size());
			return false;
		}
		
		// facilityIdが可視範囲にあるかチェック
		String facilityId = collectKeyInfoList.get(0).getFacilityid();
		if (!facilityId.equals(RoleSettingTreeConstant.ROOT_ID)) {
			boolean find = false;
			for (FacilityInfo facilityInfo : facilityUserList) {
				if (facilityInfo.getFacilityId().equals(facilityId)) {
					find = true;
					break;
				}
			}
			if (!find) {
				throw new ObjectPrivilege_InvalidRole();
			}
		}

		if (!collectKeyInfoList.get(0).getMonitorId().equals(CollectConstant.COLLECT_TYPE_JOB)) {
			// monitorIdが可視範囲にあるかチェック
			new MonitorSettingControllerBean().getMonitor(collectKeyInfoList.get(0).getMonitorId());
		} else {
			// ジョブ履歴
			String[] jobIds = collectKeyInfoList.get(0).getDisplayName().split(CollectConstant.COLLECT_TYPE_JOB_DELIMITER);
			if (jobIds.length < 2) {
				throw new InvalidRole("jobunitId:jobId=" + collectKeyInfoList.get(0).getDisplayName() + " does not exist");
			}
			// jobunitIdが可視範囲にあるかチェック
			com.clustercontrol.jobmanagement.util.QueryUtil.getJobMstPK(jobIds[0], jobIds[1]);
		}
		return true;
	}

	/**
	 * 以下の条件に一致する収集値キーの一覧を取得します。
	 *　　オーナーロールIDが参照可能
	 *　　数値監視
	 *　　指定されたファシリティIDもしくはその配下のノードに一致する
	 * 
	 * @param facilityId　ファシリティID
	 * @param ownerRoleId オーナーロールID
	 * @return 収集値キーの一覧
	 * @throws ObjectPrivilege_InvalidRole
	 * @throws MonitorNotFound
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public List<CollectKeyInfo> getCollectKeyListFindByNumFacility_OR(String facilityId, String ownerRoleId)
		throws HinemosUnknown {
		List<CollectKeyInfo> list = new ArrayList<>();
		if (facilityId == null || facilityId.isEmpty()
				|| ownerRoleId == null || ownerRoleId.isEmpty()) {
			return list;
		}
		
		// 参照可能な監視設定を取得する
		List<Integer> typeList = new ArrayList<>();
		typeList.add(MonitorTypeConstant.TYPE_NUMERIC);
		List<MonitorInfo> monitorInfoList 
			= new MonitorSettingControllerBean().getMonitorListByMonitorType(typeList, ownerRoleId);
		for (MonitorInfo monitorInfo : monitorInfoList) {
			// 指定したファシリティIDをスコープ、もしくはノードに含む場合のみ対象とする
			if (monitorInfo.getFacilityId().equals(facilityId)
					|| new RepositoryControllerBean().getFacilityIdList(
							monitorInfo.getFacilityId(), 0).contains(facilityId)) {
				list.addAll(QueryUtil.getCollectKeyInfoListByMonitorId(monitorInfo.getMonitorId()));
			}
		}
		return list;
	}
}
