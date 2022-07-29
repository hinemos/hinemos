/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
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
import org.openapitools.client.model.GetFilterNodeListRequest;
import org.openapitools.client.model.NodeInfoResponse;
import org.openapitools.client.model.NodeInfoResponseP2;

import com.clustercontrol.bean.Property;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.repository.util.NodePropertyUtil;
import com.clustercontrol.repository.util.RepositoryRestClientWrapper;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.util.RestConnectManager;
import com.clustercontrol.util.UIManager;

/**
 * 登録ノードのリストを取得するクライアント側アクションクラス<BR>
 *
 * @version 1.0.0
 * @since 1.0.0
 */
public class GetNodeList {

	// ログ
	private static Log m_log = LogFactory.getLog( GetNodeList.class );

	// ----- instance メソッド ----- //

	/**
	 * 全てのノード一覧を取得します。
	 *
	 * @param managerName マネージャ名
	 * @return ノード一覧
	 */
	public List<NodeInfoResponseP2> getAll(String managerName) {

		List<NodeInfoResponseP2> records = null;
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();

		try {
			RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(managerName);
			records = wrapper.getNodeListAll();
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
	 * propertyの条件にヒットするノードの一覧を返します。
	 *
	 * @param managerName マネージャ名
	 * @param property
	 * @return ノード一覧
	 */
	public List<NodeInfoResponseP2> get(String managerName, Property property) {
		PropertyUtil.deletePropertyDefine(property);

		List<NodeInfoResponseP2> records = null;
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();
		try {
			RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(managerName);
			NodeInfoResponse nodeInfo = null;
			nodeInfo = NodePropertyUtil.property2node(property);
			GetFilterNodeListRequest requestDto = new GetFilterNodeListRequest();
			RestClientBeanUtil.convertBean(nodeInfo, requestDto);
			records = wrapper.getFilterNodeList(requestDto);
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
	 * 全てのノード一覧を取得します。
	 *
	 * @return ノード一覧
	 */
	public Map<String, List<NodeInfoResponseP2>> getAll() {

		Map<String, List<NodeInfoResponseP2>> dispDataMap= new ConcurrentHashMap<>();
		List<NodeInfoResponseP2> records = null;
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();
		for (String managerName : RestConnectManager.getActiveManagerSet()) {
			try {
				RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(managerName);
				records = wrapper.getNodeListAll();
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

	/**
	 * propertyの条件にヒットするノードの一覧を返します。
	 *
	 * @param property
	 * @return ノード一覧
	 */
	public Map<String, List<NodeInfoResponseP2>> get(Property property) {
		PropertyUtil.deletePropertyDefine(property);

		Map<String, List<NodeInfoResponseP2>> dispDataMap= new ConcurrentHashMap<>();
		List<NodeInfoResponseP2> records = null;
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();
		for (String managerName : RestConnectManager.getActiveManagerSet()) {
			try {
				RepositoryRestClientWrapper wrapper = RepositoryRestClientWrapper.getWrapper(managerName);
				NodeInfoResponse nodeInfo = null;
				nodeInfo = NodePropertyUtil.property2node(property);
				GetFilterNodeListRequest requestDto = new GetFilterNodeListRequest();
				RestClientBeanUtil.convertBean(nodeInfo, requestDto);
				records = wrapper.getFilterNodeList(requestDto);
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
