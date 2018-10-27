/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.validation;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import com.clustercontrol.xcloud.PluginException;

public interface EntityValidator {
	public interface PropValidatorInfo {
		String propName();
		String elementId();
		Method getMethod();
		void validate(Object property, String group) throws PluginException;
	}

	public interface EntityValidationContext {
		Class<?> ｔype();
		void validate(Object entity, String group) throws PluginException;
		PropValidatorInfo getPropValidator(String propName);
		Map<String, PropValidatorInfo> getPropValidatorMap();
		List<CustomEntityValidator<?>> getCustomEntityValidators();
	}

	void validate(Object entity, String group) throws PluginException;
}
