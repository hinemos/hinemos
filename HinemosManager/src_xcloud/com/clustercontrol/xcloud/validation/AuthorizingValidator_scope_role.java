/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.validation;

import java.lang.reflect.Method;

import com.clustercontrol.xcloud.PluginException;
import com.clustercontrol.xcloud.factory.CloudManager;
import com.clustercontrol.xcloud.validation.MethodValidator.MethodValidationContext;

public class AuthorizingValidator_scope_role implements CustomMethodValidator {
	@Override
	public void validate(Method method, ParamHolder params, String group, MethodValidationContext context) throws PluginException {
		String cloudScopeId = params.getParam("XCLOUD_CORE_CLOUDSCOPE_ID", String.class);
		String ownerRoleId = params.getParam("XCLOUD_CORE_ROLE_ID", String.class);
		CloudManager.singleton().getCloudScopes().getCloudScopeByOwnerRole(cloudScopeId, ownerRoleId);
	}
}
