/*
Copyright (C) 2010 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.
 */
package com.clustercontrol.ws.cloud;

import java.io.File;
import java.util.ArrayList;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.annotation.Resource;
import javax.xml.bind.annotation.XmlMimeType;
import javax.xml.ws.WebServiceContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.FunctionConstant;
import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.SystemPrivilegeMode;
import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.hinemosagent.util.AgentConnectUtil;
import com.clustercontrol.ws.util.HttpAuthenticator;

/**
 * クラウド管理オプション共通部分用のWebAPIエンドポイント
 */
@javax.jws.WebService(targetNamespace = "http://cloud.ws.clustercontrol.com")
public class CloudCommonEndpoint {
	@Resource
	WebServiceContext wsctx;

	private static Log m_log = LogFactory.getLog(CloudCommonEndpoint.class);
	private static Log m_opelog = LogFactory.getLog("HinemosOperation");

	/**
	 * echo(WebサービスAPI疎通用)
	 * 
	 * 権限必要なし（ユーザ名チェックのみ実施）
	 * 
	 * @param str
	 * @return
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public String echo(String str) throws InvalidUserPass, InvalidRole,
			HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		return str + ", " + str;
	}

	/**
	 * @version 4.0.2
	 * @since 4.0.2
	 * 
	 * @param facilityId
	 * @throws InvalidUserPass
	 * @throws InvalidRole
	 * @throws HinemosUnknown
	 */
	public boolean sendManagerDiscoveryInfo(String facilityId) throws InvalidUserPass,
			InvalidRole, HinemosUnknown {
		m_log.debug("sendManagerIp : ");
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.CLOUDMANAGEMENT, SystemPrivilegeMode.EXEC));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		// 認証済み操作ログ
		m_opelog.debug(HinemosModuleConstant.LOG_PREFIX_REPOSITORY
				+ " Get, Method=sendManagerIp, User="
				+ HttpAuthenticator.getUserAccountString(wsctx));

		try {
			return AgentConnectUtil.sendManagerDiscoveryInfo(facilityId);
		} catch (Exception e) {
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/**
	 * [Template] 共通スクリプトを取得
	 * 
	 * HinemosAgentAccess権限が必要
	 * 
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 * @throws InvalidUserPass
	 */
	@XmlMimeType("application/octet-stream")
	public DataHandler downloadScripts(String filename) throws InvalidUserPass,
			InvalidRole, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		systemPrivilegeList.add(new SystemPrivilegeInfo(FunctionConstant.CLOUDMANAGEMENT, SystemPrivilegeMode.READ));
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		String homeDir = System.getProperty("hinemos.manager.home.dir");
		String filepath = homeDir + "/var/cloud/" + filename;
		File file = new File(filepath);
		if (!file.exists()) {
			m_log.error("file not found : " + filepath);
			return null;
		}
		FileDataSource source = new FileDataSource(file);
		DataHandler dataHandler = new DataHandler(source);
		return dataHandler;
	}
}