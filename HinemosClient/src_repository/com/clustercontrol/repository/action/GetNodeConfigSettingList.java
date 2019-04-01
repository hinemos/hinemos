/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.action;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.clustercontrol.repository.util.RepositoryEndpointWrapper;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.ws.repository.InvalidRole_Exception;
import com.clustercontrol.ws.repository.NodeConfigSettingInfo;

/**
 * 構成情報収集のリストを取得するクライアント側アクションクラス<BR>
 *
 * @version 6.2.0
 * @since 6.2.0
 */
public class GetNodeConfigSettingList {

	// ログ
	private static Log m_log = LogFactory.getLog( GetNodeConfigSettingList.class );

	// ----- instance メソッド ----- //

	/**
	 * 全ての構成情報収集設定一覧を取得します。
	 *
	 * @param managerName マネージャ名
	 * @return 構成情報収集設定一覧
	 */
	public List<NodeConfigSettingInfo> getAll(String managerName) {

		List<NodeConfigSettingInfo> records = null;
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();

		try {
			RepositoryEndpointWrapper wrapper = RepositoryEndpointWrapper.getWrapper(managerName);
			records = wrapper.getNodeConfigSettingListAll();
		} catch (InvalidRole_Exception e) {
			errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
		} catch (Exception e) {
			m_log.warn("getAll(), " + e.getMessage(), e);
			errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
		}

		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}
		return records;
	}

	/**
	 * 全ての構成情報収集設定一覧を取得します。
	 *
	 * @return 構成情報収集設定一覧
	 */
	public Map<String, List<NodeConfigSettingInfo>> getAll() {

		Map<String, List<NodeConfigSettingInfo>> dispDataMap= new ConcurrentHashMap<>();
		List<NodeConfigSettingInfo> records = null;
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();
		for (String managerName : EndpointManager.getActiveManagerSet()) {
			try {
				RepositoryEndpointWrapper wrapper = RepositoryEndpointWrapper.getWrapper(managerName);
				records = wrapper.getNodeConfigSettingListAll();
				dispDataMap.put(managerName, records);
			} catch (InvalidRole_Exception e) {
				errorMsgs.put( managerName, Messages.getString("message.accesscontrol.16") );
			} catch (Exception e) {
				m_log.warn("getAll(), " + e.getMessage(), e);
				errorMsgs.put( managerName, Messages.getString("message.hinemos.failure.unexpected") + ", " + HinemosMessage.replace(e.getMessage()));
			}
		}
		//メッセージ表示
		if( 0 < errorMsgs.size() ){
			UIManager.showMessageBox(errorMsgs, true);
		}
		return dispDataMap;
	}
}
