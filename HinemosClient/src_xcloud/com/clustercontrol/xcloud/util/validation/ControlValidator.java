/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.util.validation;

import org.eclipse.jface.dialogs.Dialog;

public interface ControlValidator {
	static final String labelKey = "labelKey";
	boolean validate(Dialog dialog) throws ValidateException, Exception;
}
