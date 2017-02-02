/*

Copyright (C) 2014 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

 */

package com.clustercontrol.infra.factory;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InfraManagementNotFound;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.infra.model.InfraCheckResult;
import com.clustercontrol.infra.util.QueryUtil;

/**
 * チェックの実行結果を取得する。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class SelectInfraCheckResult {

	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( SelectInfraCheckResult.class );

	/**
	 * @throws InfraManagementNotFound 
	 */
	public List<InfraCheckResult> getListByManagementId(String managementId) throws HinemosUnknown, InvalidUserPass, InvalidRole, InfraManagementNotFound {
		m_log.debug("getList() : start");

		m_log.debug(String.format("getList() : managementId = %s", managementId));
		
		// 環境構築情報へのアクセス権チェック
		new SelectInfraManagement().get(managementId, ObjectPrivilegeMode.READ);

		return QueryUtil.getInfraCheckResultFindByManagementId(managementId);
	}
}