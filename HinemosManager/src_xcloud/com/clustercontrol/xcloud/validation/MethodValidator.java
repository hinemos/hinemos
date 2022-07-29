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

public interface MethodValidator {
	public interface ParamValidatorInfo {
		String getParamId();
		void validate(Object parameter, String group) throws PluginException;
	}

	public interface MethodValidationContext {
		Method method();
		String validationGroup();
		ParamValidatorInfo getPramValidator(String paramId);
		ParamValidatorInfo[] getParamValidatorInfos();
		CustomMethodValidator[] getCustomMethodValidators();
	}

	void validate(Method method, Object[] params) throws PluginException;

	MethodValidationContext getMethodContext(Method method);
}
