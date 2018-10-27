/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.validation;

import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.util.Messages;

public class CommonValidatorEx extends CommonValidator {
	/**
	 * 数値の上限下限チェック
	 * @throws InvalidSetting
	 */
	public static void validateLong(String name, long i, long minSize, long maxSize) throws InvalidSetting {
		if (i < minSize || maxSize < i) {
			Object[] args = {name, Long.toString(minSize), Long.toString(maxSize)};
			throw new InvalidSetting(Messages.getString("message.common.4", args));
		}
	}
}
