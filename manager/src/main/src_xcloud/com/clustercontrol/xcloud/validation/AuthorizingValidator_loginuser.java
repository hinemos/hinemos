/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.validation;

import java.lang.reflect.Method;

import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.xcloud.PluginException;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;
import com.clustercontrol.xcloud.validation.MethodValidator.MethodValidationContext;

public class AuthorizingValidator_loginuser implements CustomMethodValidator {
	@Override
	public void validate(Method method, ParamHolder params, String group, MethodValidationContext context) throws PluginException {
		String cloudScopeId = params.getParam("XCLOUD_CORE_CLOUDSCOPE_ID", String.class);
		String cloudLoginUserId = params.getParam("XCLOUD_CORE_CLOUDLOGINUSER_ID", String.class);

		HinemosEntityManager em = Session.current().getEntityManager();
		TypedQuery<CloudLoginUserEntity> query = em.createNamedQuery("findCloudLoginUser_hinemosUser_scope", CloudLoginUserEntity.class);
		query.setParameter("userId", Session.current().getHinemosCredential().getUserId());
		query.setParameter("cloudScopeId", cloudScopeId);
		query.setParameter("loginUserId", cloudLoginUserId);
		query.setParameter("ADMINISTRATORS", RoleIdConstant.ADMINISTRATORS);
		query.setParameter("accountType", CloudLoginUserEntity.CloudUserType.account);
		
		try {
			query.getSingleResult();
		}
		catch (NoResultException e) {
			throw ErrorCode.LOGINUSER_NOT_FOUND.cloudManagerFault(cloudScopeId, cloudLoginUserId);
		}
	}
}
