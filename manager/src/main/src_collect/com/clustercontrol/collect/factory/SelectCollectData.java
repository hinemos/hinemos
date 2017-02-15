package com.clustercontrol.collect.factory;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.collect.model.CollectData;
import com.clustercontrol.collect.model.CollectKeyInfo;
import com.clustercontrol.collect.model.SummaryDay;
import com.clustercontrol.collect.model.SummaryHour;
import com.clustercontrol.collect.model.SummaryMonth;
import com.clustercontrol.collect.util.QueryUtil;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.ObjectPrivilege_InvalidRole;
import com.clustercontrol.monitor.session.MonitorSettingControllerBean;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.util.FacilityTreeCache;

public class SelectCollectData {
	
	private static Log m_log = LogFactory.getLog( SelectCollectData.class );
	
	/**
	 * IDと時間を元に収集データを取得します。
	 * 
	 * @return 収集データのリスト
	 * @throws InvalidRole 
	 * @throws HinemosUnknown 
	 * @throws MonitorNotFound 
	 * 
	 */
	public List<CollectData> getCollectDataList(List<Integer> idList, Long fromTime, Long toTime)
			throws ObjectPrivilege_InvalidRole, MonitorNotFound, HinemosUnknown, InvalidRole {
		// FIXME メソッドの引数としてオーナーロールを貰ってくるべきか要確認
		// ログインユーザでオブジェクト権限チェックしつつ収集データを取得
		String userId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
		List<FacilityInfo> facilityUserList = FacilityTreeCache.getNodeFacilityInfoListByUserId(userId); //ログインユーザが見ることができるファシリティID一覧
		List<Integer> collectidList = new ArrayList<>();
		
		for (Integer id : idList) {
			List<CollectKeyInfo> collectKeyInfoList = QueryUtil.getCollectKeyInfoList(id); //collectoridに紐づいているCollectKeyInfo
			//上記collectKeyInfoListの長さは1になる予定
			if(collectKeyInfoList.size() != 1){
				m_log.warn("getCollectDataList() collectKeyInfoList.size()= " + collectKeyInfoList.size());
				continue;
			}
			// facilityIdが可視範囲にあるかチェック
			String facilityId = collectKeyInfoList.get(0).getFacilityid();
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
			// monitorIdが可視範囲にあるかチェック
			new MonitorSettingControllerBean().getMonitor(collectKeyInfoList.get(0).getMonitorId());

			m_log.debug("collectoId:" + id);
			collectidList.add(id);
		}
		if (collectidList.size() == 0) {
			m_log.debug("collectoIdList.size() :" + collectidList.size());
			return new ArrayList<CollectData>();
		}
		return QueryUtil.getCollectDataList(collectidList, fromTime, toTime);
	}
	
	/**
	 * IDを元に収集データを取得します。
	 * 
	 * @return 収集データのリスト
	 * @throws InvalidRole 
	 * @throws HinemosUnknown 
	 * @throws MonitorNotFound 
	 * 
	 */
	public List<CollectData> getCollectDataList(Integer id) 
			throws ObjectPrivilege_InvalidRole, MonitorNotFound, HinemosUnknown, InvalidRole {
		// FIXME メソッドの引数としてオーナーロールを貰ってくるべきか要確認
		// ログインユーザでオブジェクト権限チェックしつつ収集データを取得
		String userId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
		List<FacilityInfo> facilityUserList = FacilityTreeCache.getNodeFacilityInfoListByUserId(userId); //ログインユーザが見ることができるファシリティID一覧
		List<CollectKeyInfo> collectKeyInfoList = QueryUtil.getCollectKeyInfoList(id); //collectoridに紐づいているCollectKeyInfo
		//上記facilityidListの長さは1になる予定
		if(collectKeyInfoList.size() != 1){
			m_log.warn("getCollectDataList() collectKeyInfoList.size()= " + collectKeyInfoList.size());
			return null;
		}
		
		// facilityIdが可視範囲にあるかチェック
		String facilityId = collectKeyInfoList.get(0).getFacilityid();
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
		// monitorIdが可視範囲にあるかチェック
		new MonitorSettingControllerBean().getMonitor(collectKeyInfoList.get(0).getMonitorId());

		//時間を指定しないので、collectoridに紐づくデータを全て取ってくる
		return QueryUtil.getCollectDataList(id);
	}
	
	
	/**
	 * IDと時間を元にサマリデータ(時間単位)を取得します。
	 * 
	 * @return サマリデータ(時間単位)のリスト
	 * @throws InvalidRole 
	 * @throws HinemosUnknown 
	 * @throws MonitorNotFound 
	 * 
	 */
	public List<SummaryHour> getSummaryHourList(List<Integer> idList, Long fromTime, Long toTime) 
			throws ObjectPrivilege_InvalidRole, MonitorNotFound, HinemosUnknown, InvalidRole{
		// FIXME メソッドの引数としてオーナーロールを貰ってくるべきか要確認
		// ログインユーザでオブジェクト権限チェックしつつ収集データを取得
		String userId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
		List<FacilityInfo> facilityUserList = FacilityTreeCache.getNodeFacilityInfoListByUserId(userId);//ログインユーザが見ることができるファシリティID一覧
		List<Integer> collectIdList = new ArrayList<>();
		for (Integer id : idList) {
			List<CollectKeyInfo> collectKeyInfoList = QueryUtil.getCollectKeyInfoList(id);//collectoridに紐づいているCollectKeyInfo
			//上記facilityidListの長さは1になる予定
			if(collectKeyInfoList.size() != 1){
				m_log.warn("getCollectDataList() collectKeyInfoList.size()= " + collectKeyInfoList.size());
				continue;
			}
			// facilityIdが可視範囲にあるかチェック
			String facilityId = collectKeyInfoList.get(0).getFacilityid();
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
			// monitorIdが可視範囲にあるかチェック
			new MonitorSettingControllerBean().getMonitor(collectKeyInfoList.get(0).getMonitorId());

			m_log.debug("collectoId:" + id);
			collectIdList.add(id);
		}
		if (collectIdList.size() == 0) {
			m_log.debug("collectoIdList.size() :" + collectIdList.size());
			return new ArrayList<SummaryHour>();
		}
		return QueryUtil.getSummaryHourList(collectIdList, fromTime, toTime);
	}
	
	/**
	 * IDを元にサマリデータ(時間単位)を取得します。
	 * 
	 * @return サマリデータ(時間単位)のリスト
	 * @throws InvalidRole 
	 * @throws HinemosUnknown 
	 * @throws MonitorNotFound 
	 * 
	 */
	public List<SummaryHour> getSummaryHourList(Integer id) 
			throws ObjectPrivilege_InvalidRole, MonitorNotFound, HinemosUnknown, InvalidRole{
		// FIXME メソッドの引数としてオーナーロールを貰ってくるべきか要確認
		// ログインユーザでオブジェクト権限チェックしつつ収集データを取得
		String userId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
		List<FacilityInfo> facilityUserList = FacilityTreeCache.getNodeFacilityInfoListByUserId(userId);//ログインユーザが見ることができるファシリティID一覧
		List<CollectKeyInfo> collectKeyInfoList = QueryUtil.getCollectKeyInfoList(id);//collectoridに紐づいているCollectKeyInfo
		//上記facilityidListの長さは1になる予定
		if(collectKeyInfoList.size() != 1){
			m_log.warn("getCollectDataList() collectKeyInfoList.size()= " + collectKeyInfoList.size());
			return null;
		}
		// facilityIdが可視範囲にあるかチェック
		String facilityId = collectKeyInfoList.get(0).getFacilityid();
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
		// monitorIdが可視範囲にあるかチェック
		new MonitorSettingControllerBean().getMonitor(collectKeyInfoList.get(0).getMonitorId());

		return QueryUtil.getSummaryHourList(id);
	}
	
	/**
	 * IDと時間を元にサマリデータ(日単位)を取得します。
	 * 
	 * @return サマリデータ(日単位)のリスト
	 * @throws InvalidRole 
	 * @throws HinemosUnknown 
	 * @throws MonitorNotFound 
	 * 
	 */
	public List<SummaryDay> getSummaryDayList(List<Integer> idList, Long fromTime, Long toTime) 
			throws ObjectPrivilege_InvalidRole, MonitorNotFound, HinemosUnknown, InvalidRole{
		// FIXME メソッドの引数としてオーナーロールを貰ってくるべきか要確認
		// ログインユーザでオブジェクト権限チェックしつつ収集データを取得
		String userId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
		List<FacilityInfo> facilityUserList = FacilityTreeCache.getNodeFacilityInfoListByUserId(userId); //ログインユーザが見ることができるファシリティID一覧
		List<Integer> collectIdList = new ArrayList<>();
		for (Integer id : idList) {
			List<CollectKeyInfo> collectKeyInfoList = QueryUtil.getCollectKeyInfoList(id); //collectoridに紐づいているcollectKeyInfoList
			//上記facilityidListの長さは1になる予定
			if(collectKeyInfoList.size() != 1){
				m_log.warn("getCollectDataList() collectKeyInfoList.size()= " + collectKeyInfoList.size());
				continue;
			}
			// facilityIdが可視範囲にあるかチェック
			String facilityId = collectKeyInfoList.get(0).getFacilityid();
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
			// monitorIdが可視範囲にあるかチェック
			new MonitorSettingControllerBean().getMonitor(collectKeyInfoList.get(0).getMonitorId());

			m_log.debug("collectoId:" + id);
			collectIdList.add(id);
		}
		if (collectIdList.size() == 0) {
			m_log.debug("collectoIdList.size() :" + collectIdList.size());
			return new ArrayList<SummaryDay>();
		}
		return QueryUtil.getSummaryDayList(collectIdList, fromTime, toTime);
	}
	
	/**
	 * IDを元にサマリデータ(日単位)を取得します。
	 * 
	 * @return サマリデータ(日単位)のリスト
	 * @throws InvalidRole 
	 * @throws HinemosUnknown 
	 * @throws MonitorNotFound 
	 * 
	 */
	public List<SummaryDay> getSummaryDayList(Integer id) throws ObjectPrivilege_InvalidRole, MonitorNotFound, HinemosUnknown, InvalidRole {
		// FIXME メソッドの引数としてオーナーロールを貰ってくるべきか要確認
		// ログインユーザでオブジェクト権限チェックしつつ収集データを取得
		String userId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
		List<FacilityInfo> facilityUserList = FacilityTreeCache.getNodeFacilityInfoListByUserId(userId); //ログインユーザが見ることができるファシリティID一覧
		List<CollectKeyInfo> collectKeyInfoList = QueryUtil.getCollectKeyInfoList(id); //collectoridに紐づいているcollectKeyInfoList
		//上記facilityidListの長さは1になる予定
		if(collectKeyInfoList.size() != 1){
			m_log.warn("getCollectDataList() collectKeyInfoList.size()=, " + collectKeyInfoList.size());
			return null;
		}
		// facilityIdが可視範囲にあるかチェック
		String facilityId = collectKeyInfoList.get(0).getFacilityid();
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
		// monitorIdが可視範囲にあるかチェック
		new MonitorSettingControllerBean().getMonitor(collectKeyInfoList.get(0).getMonitorId());

		return QueryUtil.getSummaryDayList(id);
	}
	
	
	/**
	 * IDと時間を元にサマリデータ(月単位)を取得します。
	 * 
	 * @return サマリデータ(月単位)のリスト
	 * @throws InvalidRole 
	 * @throws HinemosUnknown 
	 * @throws MonitorNotFound 
	 * 
	 */
	public List<SummaryMonth> getSummaryMonthList(List<Integer> idList, Long fromTime, Long toTime)
			throws ObjectPrivilege_InvalidRole, MonitorNotFound, HinemosUnknown, InvalidRole {
		// FIXME メソッドの引数としてオーナーロールを貰ってくるべきか要確認
		// ログインユーザでオブジェクト権限チェックしつつ収集データを取得
		String userId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
		List<FacilityInfo> facilityUserList = FacilityTreeCache.getNodeFacilityInfoListByUserId(userId); //ログインユーザが見ることができるファシリティID一覧
		List<Integer> collectIdList = new ArrayList<>();
		for (Integer id : idList) {
			List<CollectKeyInfo> collectKeyInfoList = QueryUtil.getCollectKeyInfoList(id); //collectoridに紐づいているcollectKeyInfoList
			//上記facilityidListの長さは1になる予定
			if(collectKeyInfoList.size() != 1){
				m_log.warn("getCollectDataList() collectKeyInfoList.size()= " + collectKeyInfoList.size());
				continue;
			}
			// facilityIdが可視範囲にあるかチェック
			String facilityId = collectKeyInfoList.get(0).getFacilityid();
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
			// monitorIdが可視範囲にあるかチェック
			new MonitorSettingControllerBean().getMonitor(collectKeyInfoList.get(0).getMonitorId());

			m_log.debug("collectoId:" + id);
			collectIdList.add(id);
		}
		if (collectIdList.size() == 0) {
			m_log.debug("collectoIdList.size() :" + collectIdList.size());
			return new ArrayList<SummaryMonth>();
		}
		return QueryUtil.getSummaryMonthList(collectIdList, fromTime, toTime);
	}
	
	/**
	 * IDを元にサマリデータ(月単位)を取得します。
	 * 
	 * @return サマリデータ(月単位)のリスト
	 * @throws InvalidRole 
	 * @throws HinemosUnknown 
	 * @throws MonitorNotFound 
	 * 
	 */
	public List<SummaryMonth> getSummaryMonthList(Integer id) throws ObjectPrivilege_InvalidRole, MonitorNotFound, HinemosUnknown, InvalidRole{
		// FIXME メソッドの引数としてオーナーロールを貰ってくるべきか要確認
		// ログインユーザでオブジェクト権限チェックしつつ収集データを取得
		String userId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
		List<FacilityInfo> facilityUserList = FacilityTreeCache.getNodeFacilityInfoListByUserId(userId); //ログインユーザが見ることができるファシリティID一覧
		List<CollectKeyInfo> collectKeyInfoList = QueryUtil.getCollectKeyInfoList(id); //collectoridに紐づいているcollectKeyInfoList
		//上記facilityidListの長さは1になる予定
		if(collectKeyInfoList.size() != 1){
			m_log.warn("getCollectDataList() collectKeyInfoList.size()= " + collectKeyInfoList.size());
			return null;
		}
		// facilityIdが可視範囲にあるかチェック
		String facilityId = collectKeyInfoList.get(0).getFacilityid();
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
		// monitorIdが可視範囲にあるかチェック
		new MonitorSettingControllerBean().getMonitor(collectKeyInfoList.get(0).getMonitorId());

		return QueryUtil.getSummaryMonthList(id);
	}
}
