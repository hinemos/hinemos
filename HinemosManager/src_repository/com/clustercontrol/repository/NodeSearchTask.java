/*

Copyright (C) 2012 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.repository;

import java.util.concurrent.Callable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.UserIdConstant;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.SnmpResponseError;
import com.clustercontrol.repository.bean.NodeInfoDeviceSearch;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.session.RepositoryControllerBean;

/**
 * ノードサーチ処理の実装クラス
 */
public class NodeSearchTask implements Callable<NodeInfoDeviceSearch> {

	private static Log m_log = LogFactory.getLog(NodeSearchTask.class);
	private String ipAddress;
	private int port;
	private String community;
	private int version;
	private String facilityID;
	private String securityLevel;
	private String user;
	private String authPass;
	private String privPass;
	private String authProtocol;
	private String privProtocol;

	/**
	 * コンストラクタ
	 * @param ipAddressTo IPアドレス
	 * @param port ポート
	 * @param community コミュニティ
	 * @param version バージョン
	 * @param facilityID ファシリティID
	 * @param securityLevel セキュリティレベル
	 * @param user ユーザー名
	 * @param authPass 認証パスワード
	 * @param privPass 暗号化パスワード
	 * @param authProtocol 認証プロトコル
	 * @param privProtocol 暗号化プロトコル
	 */
	public NodeSearchTask(String ipAddress, int port, String community,
			int version, String facilityID, String securityLevel,
			String user, String authPass, String privPass, String authProtocol,
			String privProtocol) {

		this.ipAddress = ipAddress;
		this.port = port;
		this.community = community;
		this.version = version;
		this.facilityID = facilityID;
		this.securityLevel = securityLevel;
		this.user = user;
		this.authPass = authPass;
		this.privPass = privPass;
		this.authProtocol = authProtocol;
		this.privProtocol = privProtocol;
	}

	/**
	 * ノードサーチ処理の実行
	 */
	@Override
	public NodeInfoDeviceSearch call() {
		m_log.debug("call() start");
		NodeInfoDeviceSearch searchInfo = null;
		String errorMessage = null;
		
		HinemosSessionContext.instance().setProperty(HinemosSessionContext.LOGIN_USER_ID, UserIdConstant.HINEMOS);
		RepositoryControllerBean controller = new RepositoryControllerBean();
		//リストの分だけSNMPでノード検索
		try {
			m_log.info("getNodePropertyBySNMP ipAddress=" + ipAddress);
			searchInfo = controller.getNodePropertyBySNMP(ipAddress,port, community,
					version, facilityID, securityLevel, user,authPass,
					privPass, authProtocol, privProtocol);
		} catch (HinemosUnknown | SnmpResponseError e) {
			errorMessage = "" + e.getMessage();
			searchInfo = new NodeInfoDeviceSearch();
			NodeInfo nodeInfo = new NodeInfo();
			nodeInfo.setIpAddressV4(ipAddress);
			searchInfo.setNodeInfo(nodeInfo);
			searchInfo.setErrorMessage(errorMessage);
			m_log.info("error=" + errorMessage);
		}
		return searchInfo;
	}
}
