/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.factory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.RpaManagementToolAccountDuplicate;
import com.clustercontrol.fault.RpaManagementToolAccountNotFound;
import com.clustercontrol.fault.RpaScenarioDuplicate;
import com.clustercontrol.fault.RpaScenarioNotFound;
import com.clustercontrol.rpa.model.RpaManagementToolAccount;
import com.clustercontrol.rpa.util.QueryUtil;
import com.clustercontrol.util.HinemosTime;
import com.clustercontrol.util.MessageConstant;

import jakarta.persistence.EntityExistsException;

/**
 * RPA管理ツールアカウント情報を更新するクラス<BR>
 *
 */
public class ModifyRpaAccount {
	/** ログ出力のインスタンス。 */
	private static Log m_log = LogFactory.getLog( ModifyRpaAccount.class );

	/**
	 * RPA管理ツールアカウントを作成します。
	 * 
	 * @param rpaAccount 作成対象のRPA管理ツールアカウント情報
	 * @param userId RPAシナリオ情報を作成したユーザID
	 * @throws RpaScenarioDuplicate
	 * 
	 */
	public void add(RpaManagementToolAccount rpaAccount, String userId) throws RpaManagementToolAccountDuplicate {
		long now = HinemosTime.currentTimeMillis();

		//エンティティBeanを作る
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			// 重複チェック
			jtm.checkEntityExists(RpaManagementToolAccount.class, rpaAccount.getRpaScopeId());
			rpaAccount.setRegDate(now);
			rpaAccount.setRegUser(userId);
			rpaAccount.setUpdateDate(now);
			rpaAccount.setUpdateUser(userId);
			
			em.persist(rpaAccount);
			jtm.flush();
		} catch (EntityExistsException e) {
			throw new RpaManagementToolAccountDuplicate(MessageConstant.MESSAGE_ERROR_IN_OVERLAP.getMessage(
					MessageConstant.RPA_MANAGEMENT_TOOL_ACCOUNT.getMessage(),
					MessageConstant.RPA_SCOPE_ID.getMessage()
					));
		}
	}
	
	/**
	 * RPA管理ツールアカウントを変更します。
	 * 
	 * @param rpaAccount 作成対象のRPA管理ツールアカウント情報
	 * @param userId 変更するユーザID
	 * @throws RpaScenarioDuplicate
	 * @throws RpaScenarioNotFound
	 * @throws InvalidRole
	 * 
	 */
	public void modify(RpaManagementToolAccount rpaAccount, String userId) throws RpaManagementToolAccountNotFound, InvalidRole {

		//アカウント情報を取得
		RpaManagementToolAccount entity = QueryUtil.getRpaAccountPK(rpaAccount.getRpaScopeId());

		//アカウント情報を更新
		entity.setAccountId(rpaAccount.getAccountId());
		entity.setDescription(rpaAccount.getDescription());
		entity.setDisplayName(rpaAccount.getDisplayName());
		entity.setPassword(rpaAccount.getPassword());
		entity.setTenantName(rpaAccount.getTenantName());
		entity.setProxyFlg(rpaAccount.getProxyFlg());
		entity.setProxyPassword(rpaAccount.getProxyPassword());
		entity.setProxyPort(rpaAccount.getProxyPort());
		entity.setProxyUrl(rpaAccount.getProxyUrl());
		entity.setProxyUser(rpaAccount.getProxyUser());
		entity.setRpaScopeName(rpaAccount.getRpaScopeName());
		entity.setUrl(rpaAccount.getUrl());

		entity.setUpdateDate(HinemosTime.currentTimeMillis());
		entity.setUpdateUser(userId);
	}
	
	/**
	 * RPA管理ツールアカウントを削除します。<BR>
	 * 
	 * @param rpaScopeId 削除対象のRPAスコープID
	 * @throws RpaManagementToolAccountNotFound 
	 * @throws HinemosUnknown
	 */
	public void delete(String rpaScopeId) throws RpaManagementToolAccountNotFound, InvalidRole {

		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();

			//　アカウント情報を検索し取得
			RpaManagementToolAccount entity = null;
			entity = QueryUtil.getRpaAccountPK(rpaScopeId);

			// アカウント情報を削除
			m_log.debug(String.format("delete() delete RPA Management Tool Account. rpaScopeId=%s", entity.getRpaScopeId())); 
			em.remove(entity);
		}
	}
}