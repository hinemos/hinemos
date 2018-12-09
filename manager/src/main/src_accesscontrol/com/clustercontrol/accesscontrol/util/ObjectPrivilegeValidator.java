/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.util;

import java.util.List;

import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfo;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.util.MessageConstant;

/**
 * オブジェクト権限情報の入力チェッククラス
 * 
 * @since 4.0
 */
public class ObjectPrivilegeValidator {

	/**
	 * オブジェクト権限情報(ObjectPrivilegeInfo)の基本設定の妥当性チェック
	 * @param objectPrivilegeInfo
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 */
	public static void validateObjectPrivilegeInfo(String objectType, String objectId, List<ObjectPrivilegeInfo> objectPrivilegeInfoList) throws InvalidSetting {

		// objectType
		CommonValidator.validateId(MessageConstant.OBJECT_TYPE.getMessage(), objectType, 64);

		// objectId
		CommonValidator.validateId(MessageConstant.OBJECT_ID.getMessage(), objectId, 64);

		for (ObjectPrivilegeInfo objectPrivilegeInfo : objectPrivilegeInfoList) {
			// roleId
			CommonValidator.validateId(MessageConstant.ROLE_ID.getMessage(), objectPrivilegeInfo.getRoleId(), 64);

			// objectPrivilege
			CommonValidator.validateId(MessageConstant.OBJECT_PRIVILEGE.getMessage(), objectPrivilegeInfo.getObjectPrivilege(), 64);
		}
	}


}
