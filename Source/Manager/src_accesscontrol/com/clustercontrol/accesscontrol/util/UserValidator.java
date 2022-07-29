/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.util;

import com.clustercontrol.accesscontrol.model.UserInfo;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.util.MessageConstant;

/**
 * ユーザ管理の入力チェッククラス
 * 
 * @since 4.0
 */
public class UserValidator {

	/**
	 * ユーザ情報(UserInfo)の基本設定の妥当性チェック
	 * @param userInfo
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 */
	public static void validateUserInfo(UserInfo userInfo) throws InvalidSetting {

		// userId
		CommonValidator.validateId(MessageConstant.USER_ID.getMessage(), userInfo.getUserId(), 64);

		// userName
		CommonValidator.validateString(MessageConstant.USER_NAME.getMessage(), userInfo.getUserName(), true, 1, 128);

		// description
		CommonValidator.validateString(MessageConstant.DESCRIPTION.getMessage(), userInfo.getDescription(), false, 0, 256);

		// mailAddress
		CommonValidator.validateString(MessageConstant.MAIL_ADDRESS.getMessage(), userInfo.getMailAddress(), false, 0, 1024);

	}


}
