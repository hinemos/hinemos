/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
		new SelectInfraManagement().get(managementId, null, ObjectPrivilegeMode.READ);

		return QueryUtil.getInfraCheckResultFindByManagementId(managementId);
	}
}