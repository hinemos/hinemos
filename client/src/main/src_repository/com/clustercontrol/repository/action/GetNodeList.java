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
import com.clustercontrol.bean.Property;
import com.clustercontrol.repository.util.NodePropertyUtil;
import com.clustercontrol.repository.util.RepositoryEndpointWrapper;
import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.HinemosMessage;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.PropertyUtil;
import com.clustercontrol.util.UIManager;
import com.clustercontrol.ws.repository.InvalidRole_Exception;
import com.clustercontrol.ws.repository.NodeInfo;

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
	public List<NodeInfo> getAll(String managerName) {

		List<NodeInfo> records = null;
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();

		try {
			RepositoryEndpointWrapper wrapper = RepositoryEndpointWrapper.getWrapper(managerName);
			records = wrapper.getNodeListAll();
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
	 * propertyの条件にヒットするノードの一覧を返します。
	 *
	 * @param managerName マネージャ名
	 * @param property
	 * @return ノード一覧
	 */
	public List<NodeInfo> get(String managerName, Property property) {
		PropertyUtil.deletePropertyDefine(property);

		List<NodeInfo> records = null;
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();
		try {
			RepositoryEndpointWrapper wrapper = RepositoryEndpointWrapper.getWrapper(managerName);
			NodeInfo nodeInfo = null;
			nodeInfo = NodePropertyUtil.property2node(property);
			records = wrapper.getFilterNodeList(nodeInfo);
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
	 * 全てのノード一覧を取得します。
	 *
	 * @return ノード一覧
	 */
	public Map<String, List<NodeInfo>> getAll() {

		Map<String, List<NodeInfo>> dispDataMap= new ConcurrentHashMap<>();
		List<NodeInfo> records = null;
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();
		for (String managerName : EndpointManager.getActiveManagerSet()) {
			try {
				RepositoryEndpointWrapper wrapper = RepositoryEndpointWrapper.getWrapper(managerName);
				records = wrapper.getNodeListAll();
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

	/**
	 * propertyの条件にヒットするノードの一覧を返します。
	 *
	 * @param property
	 * @return ノード一覧
	 */
	public Map<String, List<NodeInfo>> get(Property property) {
		PropertyUtil.deletePropertyDefine(property);

		Map<String, List<NodeInfo>> dispDataMap= new ConcurrentHashMap<>();
		List<NodeInfo> records = null;
		Map<String, String> errorMsgs = new ConcurrentHashMap<>();
		for (String managerName : EndpointManager.getActiveManagerSet()) {
			try {
				RepositoryEndpointWrapper wrapper = RepositoryEndpointWrapper.getWrapper(managerName);
				NodeInfo nodeInfo = null;
				nodeInfo = NodePropertyUtil.property2node(property);
				records = wrapper.getFilterNodeList(nodeInfo);
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
