/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.util;

import jakarta.persistence.TypedQuery;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.commons.util.HinemosSessionContext;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;

public class AuthorizingUtil {

	public static boolean checkHinemousUser_administrators_account(String hinemousUserId, String cloudScopeId) throws CloudManagerException {
		if (
			!Boolean.TRUE.equals(HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR))
			) {

			HinemosEntityManager em = Session.current().getEntityManager();
			TypedQuery<CloudLoginUserEntity> query = em.createNamedQuery("findCloudLoginUser_account", CloudLoginUserEntity.class);
			query.setParameter("userId", hinemousUserId);
			query.setParameter("cloudScopeId", cloudScopeId);
			query.setParameter("accountType", CloudLoginUserEntity.CloudUserType.account);

			return !query.getResultList().isEmpty();
		}
		else return true;
	}

	public static boolean checkHinemousUser_administrators_account_self(String hinemousUserId, String cloudScopeId, String loginUserId) throws CloudManagerException {
		if (
			!Boolean.TRUE.equals(HinemosSessionContext.instance().getProperty(HinemosSessionContext.IS_ADMINISTRATOR))
			) {

			HinemosEntityManager em = Session.current().getEntityManager();
			TypedQuery<CloudLoginUserEntity> query = em.createNamedQuery("findCloudLoginUser_account_self", CloudLoginUserEntity.class);
			query.setParameter("userId", Session.current().getHinemosCredential().getUserId());
			query.setParameter("cloudScopeId", cloudScopeId);
			query.setParameter("loginUserId", loginUserId);
			query.setParameter("accountType", CloudLoginUserEntity.CloudUserType.account);

			return !query.getResultList().isEmpty();
		}
		else return true;
	}
}
