/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.validation;

import com.clustercontrol.xcloud.PluginException;

public interface CustomEntityValidator<T> {
	void validate(T entity, String group, EntityValidator.EntityValidationContext context) throws PluginException;
}
