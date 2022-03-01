/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rpa.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.JpaTransactionCallback;
import com.clustercontrol.commons.util.JpaTransactionManager;
import com.clustercontrol.rpa.model.RpaManagementToolAccount;
import com.clustercontrol.rpa.session.RpaResourceMonitor;

/**
 * RPA管理ツールアカウントが変更された際、リソースの自動検知を行うコールバックメソッドを定義するクラス
 */
public class RpaResourceDetectCallback implements JpaTransactionCallback {
	private static Log m_log = LogFactory.getLog(RpaResourceDetectCallback.class);

	private String rpaScopeId;

	public RpaResourceDetectCallback(String rpaScopeId) {
		super();
		this.rpaScopeId = rpaScopeId;
	}

	@Override
	public void preFlush() {
	}

	@Override
	public void postFlush() {
	}

	@Override
	public void preCommit() {
	}

	@Override
	public void postCommit() {
		RpaManagementToolAccount rpaManagementToolAccount = null;
		try (JpaTransactionManager jtm = new JpaTransactionManager()) {
			HinemosEntityManager em = jtm.getEntityManager();
			rpaManagementToolAccount = QueryUtil.getRpaAccountPK(rpaScopeId, ObjectPrivilegeMode.NONE);
			em.refresh(rpaManagementToolAccount);
			RpaResourceMonitor.resourceUpdate(rpaManagementToolAccount);
		} catch (Exception e) {
			m_log.warn("postCommit(): update RPA resource failed. :"
					+ " pk=" + rpaScopeId , e);
		}
	}

	@Override
	public void preRollback() {
	}

	@Override
	public void postRollback() {
	}

	@Override
	public void preClose() {
	}

	@Override
	public void postClose() {
	}

}
