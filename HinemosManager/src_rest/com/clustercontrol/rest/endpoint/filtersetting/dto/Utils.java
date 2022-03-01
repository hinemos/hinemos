/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.filtersetting.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.filtersetting.bean.FilterSettingConstant;
import com.clustercontrol.util.FilterConstant;
import com.clustercontrol.util.MessageConstant;

/*package scope*/ class Utils {

	/**
	 * (Logical)AND結合数のバリデーションを行います。
	 * 
	 * @param itemName チェック対象項目の名前。
	 * @param value チェックする値。
	 * @throws InvalidSetting バリデーションエラー。
	 */
	public static void validateLAndConjunction(String itemName, String value) throws InvalidSetting {
		if (value == null) {
			return;
		}
		// AND結合文字列にはエスケープ仕様はないので、単純に分割して数える
		int nVal = value.split(FilterConstant.AND_SEPARATOR).length;
		if (nVal > FilterSettingConstant.ITEM_COUNT_MAX) {
			throw new InvalidSetting(
					MessageConstant.MESSAGE_FLTSET_LAND_EXCEEDED.getMessage(
							String.valueOf(FilterSettingConstant.ITEM_COUNT_MAX),
							itemName));
		}
	}

	/**
	 * (Logical)AND結合数のバリデーションを行います。
	 * 
	 * @param itemName チェック対象項目の名前。
	 * @param value チェックする値。
	 * @throws InvalidSetting バリデーションエラー。
	 */
	public static void validateLAndConjunction(MessageConstant itemName, String value) throws InvalidSetting {
		validateLAndConjunction(itemName.toString(), value);
	}
}
