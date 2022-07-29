/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.accesscontrol.util;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.accesscontrol.model.ObjectPrivilegeInfo;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.RoleNotFound;
import com.clustercontrol.repository.factory.FacilitySelector;
import com.clustercontrol.util.MessageConstant;

/**
 * オブジェクト権限情報の入力チェッククラス
 * 
 * @since 4.0
 */
public class ObjectPrivilegeValidator {

	private static Log m_log = LogFactory.getLog( ObjectPrivilegeValidator.class );

	/**
	 * オブジェクト権限情報(ObjectPrivilegeInfo)の基本設定の妥当性チェック
	 * @param objectPrivilegeInfo
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 */
	public static void validateObjectPrivilegeInfo(String objectType, String objectId, List<ObjectPrivilegeInfo> objectPrivilegeInfoList) throws InvalidSetting {

		// objectType
		CommonValidator.validateId(MessageConstant.OBJECT_TYPE.getMessage(), objectType, 64);
		// オブジェクト権限を割り当て可能なオブジェクトかどうかをチェック
		if (!ObjectPrivilegeUtil.isObjectPrivilegeObject(objectType)) {
			InvalidSetting e = new InvalidSetting("The objectType is invalid. objectType=" + objectType);
			m_log.warn("validateObjectPrivilegeInfo() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}

		// objectId
		CommonValidator.validateId(MessageConstant.OBJECT_ID.getMessage(), objectId, 512);
		// リポジトリの場合、対象がスコープかどうかチェック
		if (HinemosModuleConstant.PLATFORM_REPOSITORY.equals(objectType)) {
			boolean isScope = false;
			try {
				isScope = FacilitySelector.isScope(objectId);
			} catch (FacilityNotFound e) {
				throw new InvalidSetting(e.getMessage(), e);
			}
			if (!isScope) {
				InvalidSetting e = new InvalidSetting("Target is not Scope. facilityId=" + objectId);
				m_log.warn("validateObjectPrivilegeInfo() : " + e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
		}

		for (ObjectPrivilegeInfo objectPrivilegeInfo : objectPrivilegeInfoList) {
			// roleId
			CommonValidator.validateId(MessageConstant.ROLE_ID.getMessage(), objectPrivilegeInfo.getRoleId(), 64);
			// ロールが存在するかチェック
			try {
				com.clustercontrol.accesscontrol.util.QueryUtil.getRolePK(objectPrivilegeInfo.getRoleId());
			} catch (RoleNotFound e) {
				throw new InvalidSetting(e.getMessage(), e);
			}

			// objectPrivilege
			CommonValidator.validateId(MessageConstant.OBJECT_PRIVILEGE.getMessage(), objectPrivilegeInfo.getObjectPrivilege(), 64);
		}
	}


}
