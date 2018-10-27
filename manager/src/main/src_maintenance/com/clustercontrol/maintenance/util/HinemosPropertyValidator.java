/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.maintenance.util;

import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.maintenance.HinemosPropertyTypeConstant;
import com.clustercontrol.maintenance.model.HinemosPropertyInfo;
import com.clustercontrol.util.MessageConstant;

/**
 * 共通設定情報の入力チェッククラス
 *
 * @version 5.0.0
 * @since 5.0
 */
public class HinemosPropertyValidator {

	/**
	 * 共通設定情報の妥当性チェック
	 *
	 * @param info
	 * @throws InvalidSetting
	 */
	public static void validateHinemosPropertyInfo(HinemosPropertyInfo info) throws InvalidSetting {

		// key
		//nullチェックあり
		CommonValidator.validateString(MessageConstant.HINEMOS_PROPERTY_KEY.getMessage(),
				info.getKey(), true, 1, 64);

		//value
		if (info.getValueType() == HinemosPropertyTypeConstant.TYPE_STRING
				&& info.getValueString() != null && info.getValueString().trim().length() > 0) {
			CommonValidator.validateString(MessageConstant.HINEMOS_PROPERTY_VALUE.getMessage(),
					info.getValueString(), false, 0, 1024);
		} else if (info.getValueType() == HinemosPropertyTypeConstant.TYPE_NUMERIC && info.getValueNumeric() != null) {
			CommonValidator.validateLong(
					MessageConstant.HINEMOS_PROPERTY_VALUE.getMessage(),
					info.getValueNumeric(), Long.MIN_VALUE,
					Long.MAX_VALUE);
		}

		// description
		CommonValidator.validateString(MessageConstant.DESCRIPTION.getMessage(),info.getDescription(), false, 0, 512);
	}
}
