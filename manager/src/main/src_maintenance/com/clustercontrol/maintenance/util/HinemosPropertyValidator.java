/*

Copyright (C) since 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
