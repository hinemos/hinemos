/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.validation;

import java.lang.reflect.Method;

import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import com.clustercontrol.accesscontrol.bean.RoleIdConstant;
import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.xcloud.PluginException;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.validation.MethodValidator.MethodValidationContext;

public class AuthorizingValidator_scope_admin implements CustomMethodValidator {
	@Override
	public void validate(Method method, ParamHolder params, String group, MethodValidationContext context) throws PluginException {
		String cloudScopeId = params.getParam("XCLOUD_CORE_CLOUDSCOPE_ID", String.class);

		HinemosEntityManager em = Session.current().getEntityManager();
		TypedQuery<CloudScopeEntity> query = em.createNamedQuery("findCloudScopeByHinemosUserAsAdmin", CloudScopeEntity.class);
		query.setParameter("userId", Session.current().getHinemosCredential().getUserId());
		query.setParameter("cloudScopeId", cloudScopeId);
		query.setParameter("ADMINISTRATORS", RoleIdConstant.ADMINISTRATORS);
		query.setParameter("accountType", CloudLoginUserEntity.CloudUserType.account);
		
		try {
			query.getSingleResult();
		}
		catch (NoResultException e) {
			throw ErrorCode.CLOUDSCOPE_INVALID_CLOUDSCOPE_NOT_FOUND.cloudManagerFault(cloudScopeId);
		}
	}
}
