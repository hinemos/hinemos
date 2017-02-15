package com.clustercontrol.collect.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.collect.model.CollectKeyInfoPK;
import com.clustercontrol.collect.util.QueryUtil;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.fault.CollectKeyNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.monitor.run.model.MonitorInfo;
import com.clustercontrol.monitor.session.MonitorSettingControllerBean;
import com.clustercontrol.repository.model.FacilityInfo;
import com.clustercontrol.repository.util.FacilityTreeCache;

public class SelectCollectKeyInfo {
	
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog(SelectCollectKeyInfo.class);
	
	/**
	 * 収集項目IDを取得します。
	 * @throws CollectKeyNotFound 
	 * @throws InvalidRole 
	 * @throws HinemosUnknown 
	 * @throws MonitorNotFound 
	 */
	public Integer getCollectId(String itemName, String displayName, String monitorId, String facilityId) 
			throws CollectKeyNotFound, MonitorNotFound, HinemosUnknown, InvalidRole {
		String userId = (String)HinemosSessionContext.instance().getProperty(HinemosSessionContext.LOGIN_USER_ID);
			
		// monitorIdが可視範囲にあるかチェック
		new MonitorSettingControllerBean().getMonitor(monitorId);

		// facilityIdが可視範囲にあるかチェック
		List<FacilityInfo> facilityList = FacilityTreeCache.getNodeFacilityInfoListByUserId(userId);
		m_log.debug("userId:" + userId + ", facilityList:" + facilityList.toString());
		
		boolean find = false;
		for (FacilityInfo facilityInfo : facilityList) {
			if (facilityInfo.getFacilityId().equals(facilityId)) {
				find = true;
				break;
			}
		}
		if (!find) {
			throw new InvalidRole("facilityId=" + facilityId + " does not exist");
		}
		
		CollectKeyInfoPK pk = new CollectKeyInfoPK(itemName, displayName, monitorId, facilityId);
		return QueryUtil.getCollectKeyPK(pk).getCollectorid();
	}
	
	/**
	 * 収集項目コードを取得します。
	 * 
	 * @return 収集項目コードのリスト
	 * @throws HinemosUnknown 
	 * @throws InvalidRole 
	 * 
	 */
	public List<CollectKeyInfoPK> getCollectKeyList(List<String> facilityIdList) throws InvalidRole, HinemosUnknown {
		List<CollectKeyInfoPK> retCollectList = new ArrayList<>();
		List<CollectKeyInfoPK> collectKeyList = QueryUtil.getCollectKeyAll();

		// monitorIdが可視範囲にあるかチェック
		ArrayList<MonitorInfo> monitorList = new MonitorSettingControllerBean().getMonitorList();
		HashMap<String, String> facilityIdMap = new HashMap<String, String> ();
		
		// listからmapに変換する。
		for (String facilityId : facilityIdList) {
			facilityIdMap.put(facilityId, facilityId);
		}
		
		for (CollectKeyInfoPK info : collectKeyList) {
			if (facilityIdMap.get(info.getFacilityid()) == null) {
				continue;
			}
			for (MonitorInfo monitorInfo : monitorList) {
				if (monitorInfo.getMonitorId().equals(info.getMonitorId())) {
					retCollectList.add(info);
				}
			}
		}
		return retCollectList;
	}
}
