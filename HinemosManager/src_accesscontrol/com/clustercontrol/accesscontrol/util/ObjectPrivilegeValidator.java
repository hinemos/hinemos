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
