/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.infra.util;

import java.util.HashSet;
import java.util.Set;

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
import com.clustercontrol.infra.model.InfraManagementParamInfo;
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
		if(infraManagementInfo.getFacilityId() == null){
			infraManagementInfo.setFacilityId(null);
			infraManagementInfo.setScope(null);
		}else{
			if("".equals(infraManagementInfo.getFacilityId())){
				InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_SCOPE.getMessage());
				m_log.info("validateInfraManagementInfo() : "
						+ e.getClass().getSimpleName() + ", " + e.getMessage());
				throw e;
			}
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

		// parameterInfo
		if (infraManagementInfo.getInfraManagementParamList() != null) {
			for (InfraManagementParamInfo paramInfo : infraManagementInfo.getInfraManagementParamList()) {
				CommonValidator.validateString(MessageConstant.INFRA_PARAM_ID.getMessage(), paramInfo.getParamId(), true, 1, 64);
				// IDのパターンと":"以外は許容しない
				String pattern = "^[A-Za-z0-9-_.@:]+$";
				if(!paramInfo.getParamId().matches(pattern)){
					InvalidSetting e = new InvalidSetting(MessageConstant.MESSAGE_INPUT_PARAMID_ILLEGAL_CHARACTERS.getMessage(
							MessageConstant.INFRA_PARAM_ID.getMessage(), paramInfo.getParamId()));
					m_log.info("validateId() : "
							+ e.getClass().getSimpleName() + ", " + e.getMessage());
					throw e;
				}
				CommonValidator.validateString(MessageConstant.DESCRIPTION.getMessage(), paramInfo.getDescription(), false, 0, 256);
				CommonValidator.validateString(MessageConstant.INFRA_PARAM_VALUE.getMessage(), paramInfo.getValue(), true, 1, 1024);
			}
		}
		
		// notifyId
		if(infraManagementInfo.getNotifyRelationList() != null){
			for(NotifyRelationInfo notifyRelationInfo: infraManagementInfo.getNotifyRelationList()){
				CommonValidator.validateNotifyId(notifyRelationInfo.getNotifyId(), true, infraManagementInfo.getOwnerRoleId());
			}
		}
		
		for (InfraModuleInfo<?> infraModuleInfo: infraManagementInfo.getModuleList()) {
			infraModuleInfo.validate(infraManagementInfo);
		}
		
		// 順序の重複チェック
		Set<Integer> orderNoSet = new HashSet<Integer>();
		for (InfraModuleInfo<?> infraModuleInfo: infraManagementInfo.getModuleList()) {
			if (orderNoSet.contains(infraModuleInfo.getOrderNo())) {
				throw new InvalidSetting(MessageConstant.MESSAGE_DUPLICATED.getMessage(
						"order_no", String.valueOf(infraModuleInfo.getOrderNo())));
			}
			orderNoSet.add(infraModuleInfo.getOrderNo());
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
			InvalidSetting e1 = new InvalidSetting(MessageConstant.MESSAGE_PLEASE_SET_INFRA_MANAGEMENT.getMessage() +
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
