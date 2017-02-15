/*

 Copyright (C) 2014 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

 */
package com.clustercontrol.infra.util;

import org.apache.log4j.Logger;

import com.clustercontrol.accesscontrol.bean.PrivilegeConstant.ObjectPrivilegeMode;
import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.infra.model.InfraFileInfo;
import com.clustercontrol.infra.model.InfraManagementInfo;
import com.clustercontrol.infra.model.InfraModuleInfo;
import com.clustercontrol.notify.model.NotifyRelationInfo;
import com.clustercontrol.repository.util.FacilityTreeCache;
import com.clustercontrol.util.MessageConstant;

/**
 * 環境構築機能情報をバリデーションする。
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class InfraManagementValidator {
	private static Logger m_log = Logger.getLogger(InfraManagementValidator.class);

	
	/**
	 * 環境構築機能情報のvalidate
	 * 
	 * @param jobSchedule
	 * @throws InvalidSetting
	 * @throws HinemosUnknown
	 * @throws InvalidRole
	 */
	public static void validateInfraManagementInfo (InfraManagementInfo infraManagementInfo) throws InvalidSetting, InvalidRole {
		if(infraManagementInfo == null){
			InvalidSetting e = new InvalidSetting("InfraManagementInfo is not defined.");
			m_log.info("validate() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		
		//infraManagementInfo.validate();

		// managementId
		CommonValidator.validateId(MessageConstant.INFRA_MANAGEMENT_ID.getMessage(), infraManagementInfo.getManagementId(), 64);
		
		// name
		CommonValidator.validateString(MessageConstant.INFRA_MANAGEMENT_NAME.getMessage(), infraManagementInfo.getName(), true, 1, 64);
		
		// description
		CommonValidator.validateString(MessageConstant.INFRA_MANAGEMENT_DESCRIPTION.getMessage(), infraManagementInfo.getDescription(), false, 0, 256);
		
		// ownerRoleId
		CommonValidator.validateOwnerRoleId(infraManagementInfo.getOwnerRoleId(), true, infraManagementInfo.getManagementId(), HinemosModuleConstant.INFRA);

		// facilityId
		if(infraManagementInfo.getFacilityId() == null || "".equals(infraManagementInfo.getFacilityId())){
			infraManagementInfo.setFacilityId(null);
			infraManagementInfo.setScope(null);
		}else{
			try {
				FacilityTreeCache.validateFacilityId(infraManagementInfo.getFacilityId(), infraManagementInfo.getOwnerRoleId(), false);
			} catch (FacilityNotFound e) {
				throw new InvalidSetting(e.getMessage(), e);
			}

			// scope
			if (infraManagementInfo.getScope() != null) {
				CommonValidator.validateString(MessageConstant.SCOPE.getMessage(), infraManagementInfo.getScope(), true, 1, 64);
			}
		}
		
		// validFlg : not implemented
		// notifyGroupID : not implemented			
		// startPriority : not implemented
		// normalPriorityRun : not implemented
		// abnormalPriorityRun : not implemented
		// normalPriorityCheck : not implemented
		// abnormalPriorityCheck : not implemented
		
		// notifyId
		if(infraManagementInfo.getNotifyRelationList() != null){
			for(NotifyRelationInfo notifyRelationInfo: infraManagementInfo.getNotifyRelationList()){
				CommonValidator.validateNotifyId(notifyRelationInfo.getNotifyId(), true, infraManagementInfo.getOwnerRoleId());
			}
		}
		
		for (InfraModuleInfo<?> infraModuleInfo: infraManagementInfo.getModuleList()) {
			infraModuleInfo.validate(infraManagementInfo);
		}
	}
	
	public static void validateInfraFileInfo (InfraFileInfo infraFileInfo) throws InvalidSetting, InvalidRole {
		if(infraFileInfo == null){
			InvalidSetting e = new InvalidSetting("InfraFlieInfo is not defined.");
			m_log.info("validate() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}
		
		//protected byte[] fileContent;
		// fileId
		CommonValidator.validateId(MessageConstant.INFRA_FILEMANAGER_FILE_ID.getMessage(), infraFileInfo.getFileId(), 256);
		
		//fileName
		CommonValidator.validateString(MessageConstant.INFRA_FILEMANAGER_FILE_NAME.getMessage(), infraFileInfo.getFileName(), true, 1, 256);
		
		//protected String ownerRoleId;
		CommonValidator.validateOwnerRoleId(infraFileInfo.getOwnerRoleId(), true, infraFileInfo.getFileId(), HinemosModuleConstant.INFRA);

		//uploadDatetime : not implemented
		//uploadUserId : not implemented
		
	}
	
	/**
	 * 構築IDの存在チェック(オーナーロールIDによるチェック)
	 * @param managementId
	 * @param ownerRoleId
	 * @throws InvalidSetting
	 * @throws InvalidRole
	 */
	public static void validateInfraManagementId(String managementId, String ownerRoleId) throws InvalidSetting, InvalidRole {
		try {
			QueryUtil.getInfraManagementInfoPK_OR(managementId, ownerRoleId);
		} catch (InvalidRole e) {
			throw e;
		} catch (Exception e) {
			m_log.info("validateInfraManagementId() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_JOB.getMessage() +
					" Target construct is not exist! managementId = " + managementId);
			throw e1;
		}
	}
	
	/**
	 * 構築IDの存在チェック
	 * @param managementId
	 * @param isFlag true:参照権限関係無しに全件検索 false : 通常時
	 * @throws InvalidRole 
	 * @throws InvalidSetting 
	 */
	public static void validateInfraManagementId(String managementId, boolean isFlag) throws InvalidRole, InvalidSetting {
		try {
			//参照権限関係無しに全件検索する場合
			if (isFlag) {
				QueryUtil.getInfraManagementInfoPK(managementId, ObjectPrivilegeMode.NONE);
			}
			//参照権限あり
			else {
				QueryUtil.getInfraManagementInfoPK(managementId);
			}
		} catch (InvalidRole e) {
			throw e;
		} catch (Exception e) {
			m_log.info("validateJobId() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_INFRA_MANAGEMENT.getMessage() +
					" Target managementId is not exist! managementId = " + managementId);
			throw e1;
		}
	}
}
