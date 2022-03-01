/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.factory;

import java.util.List;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.RpaManagementToolAccountNotFound;
import com.clustercontrol.fault.RpaScenarioNotFound;
import com.clustercontrol.rpa.model.RpaManagementToolAccount;
import com.clustercontrol.rpa.util.QueryUtil;

/**
 * RPA管理ツールアカウント情報を検索するクラス<BR>
 *
 */
public class SelectRpaAccount {


	/**
	 * RPA管理ツールアカウント情報を返します。
	 * 
	 * @param rpaScopeId 取得対象のRPAスコープID
	 * @param userId 検索ユーザID
	 * @return RPAスコープ情報
	 * @throws RpaScenarioNotFound
	 * @throws InvalidRole
	 * 
	 */
	public RpaManagementToolAccount getRpaAccount(String rpaScopeId) throws RpaManagementToolAccountNotFound, InvalidRole {
		if (rpaScopeId == null) {
			return null;
		}
		return QueryUtil.getRpaAccountPK(rpaScopeId);
	}

	/**
	 * RPA管理ツールアカウント一覧を返します。
	 * 
	 * @param userId 検索ユーザID
	 * @return RPAスコープ情報
	 * @throws RpaScenarioNotFound
	 * @throws InvalidRole
	 * 
	 */
	public List<RpaManagementToolAccount> getRpaAccountList() {
		return QueryUtil.getRpaAccountList();
	}

}
