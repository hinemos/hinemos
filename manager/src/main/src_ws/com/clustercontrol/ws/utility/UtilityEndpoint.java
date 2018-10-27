/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.ws.utility;

import java.util.ArrayList;

import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;

import com.clustercontrol.accesscontrol.model.SystemPrivilegeInfo;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.util.KeyCheck;
import com.clustercontrol.ws.util.HttpAuthenticator;

/**
 * Utility用のWebAPIエンドポイント
 */
@javax.jws.WebService(targetNamespace = "http://utility.ws.clustercontrol.com")
public class UtilityEndpoint {
	@Resource
	WebServiceContext wsctx;

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
	public String echo(String str) throws InvalidUserPass, InvalidRole, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		return str + ", " + str;
	}

	public String getVersion() throws InvalidUserPass, InvalidRole, HinemosUnknown {
		ArrayList<SystemPrivilegeInfo> systemPrivilegeList = new ArrayList<SystemPrivilegeInfo>();
		HttpAuthenticator.authCheck(wsctx, systemPrivilegeList);

		return KeyCheck.getResultEnterprise();
	}
}
