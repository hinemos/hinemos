/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.validation;

import java.lang.annotation.Annotation;

import com.clustercontrol.xcloud.PluginException;

public interface Validator<A extends Annotation, T> {
	String getElementId();
	void setElementId(String elementId);
	String getValidationId();
	void setValidationId(String validationId);
	String[] getGroups();
	void setGroups(String[] groups);
	void init(A annotation);
	void validate(T property, String group) throws PluginException;
}
