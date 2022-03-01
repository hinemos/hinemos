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
import org.openapitools.client.model.NodeConfigSettingInfoResponse;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.UIManager;

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
	public List<NodeConfigSettingInfoResponse> getAll(String managerName) {

		List<NodeConfigSettingInfoResponse> records = null;
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();

		try {
			RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(managerName);
			records = wrapper.getNodeConfigSettingList();
		} catch (InvalidRole e) {
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
	public Map<String, List<NodeConfigSettingInfoResponse>> getAll() {

		Map<String, List<NodeConfigSettingInfoResponse>> dispDataMap= new ConcurrentHashMap<>();
		List<NodeConfigSettingInfoResponse> records = null;
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();
		for (String managerName : RestConnectManager.getActiveManagerSet()) {
			try {
				RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(managerName);
				records = wrapper.getNodeConfigSettingList();
				dispDataMap.put(managerName, records);
			} catch (InvalidRole e) {
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
