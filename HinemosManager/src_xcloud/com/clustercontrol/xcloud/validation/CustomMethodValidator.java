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

public interface CustomMethodValidator {
	void validate(Method method, ParamHolder params, String group, MethodValidator.MethodValidationContext context) throws PluginException;
}
