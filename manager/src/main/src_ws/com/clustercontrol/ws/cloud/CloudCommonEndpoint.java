/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.ws.cloud;

import java.util.ArrayList;

import javax.annotation.Resource;
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
}