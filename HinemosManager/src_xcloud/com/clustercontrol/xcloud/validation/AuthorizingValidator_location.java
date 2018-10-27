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
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.validation.MethodValidator.MethodValidationContext;

public class AuthorizingValidator_location implements CustomMethodValidator {
	@Override
	public void validate(Method method, ParamHolder params, String group, MethodValidationContext context) throws PluginException {
		String cloudScopeId = params.getParam("XCLOUD_CORE_CLOUDSCOPE_ID", String.class);
		String locationId = params.getParam("XCLOUD_CORE_LOCATION_ID", String.class);
		CloudScopeEntity scope = CloudManager.singleton().getCloudScopes().getCloudScopeByCurrentHinemosUser(cloudScopeId);
		scope.getLocation(locationId);
	}
}
