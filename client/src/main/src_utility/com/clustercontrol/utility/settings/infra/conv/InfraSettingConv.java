/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.infra.conv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.clustercontrol.jobmanagement.bean.SystemParameterConstant;
import com.clustercontrol.utility.settings.infra.xml.FileTransferVariable;
import com.clustercontrol.utility.settings.infra.xml.InfraManagementInfo;
import com.clustercontrol.utility.settings.infra.xml.InfraManagementParam;
import com.clustercontrol.utility.settings.infra.xml.InfraModuleInfo;
import com.clustercontrol.utility.settings.infra.xml.NotifyId;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.util.DateUtil;
import com.clustercontrol.ws.infra.CommandModuleInfo;
import com.clustercontrol.ws.infra.FileTransferModuleInfo;
import com.clustercontrol.ws.infra.InfraManagementParamInfo;
import com.clustercontrol.ws.infra.ReferManagementModuleInfo;

/**
 * 環境構築設定情報をJavaBeanとXML(Bean)のbindingとの間でやりとりを
 * 行うクラス<BR>
 * 
 * @version 6.1.0
 * @since 5.0.a
 */
public class InfraSettingConv extends BaseConv {
	
	private static final String ExecModule = "ExecModule";
	private static final String FileTransferModule = "FileTransferModule";
	private static final String ReferModule = "ReferManagementModule";
	
	// スキーマのタイプ、バージョン、リビジョンをそれぞれ返す
	@Override
	protected String getType() {return "I";}
	@Override
	protected String getVersion() {return "1";}
	@Override
	protected String getRevision() {return "1";}
	
	/**
	 * DTOのBeanからXMLのBeanに変換する。
	 * 
	 * @param info　DTOのBean
	 * @return
	 * @throws Exception
	 */
	public InfraManagementInfo getXmlInfo(com.clustercontrol.ws.infra.InfraManagementInfo info) throws Exception {

		InfraManagementInfo ret = new InfraManagementInfo();

		//情報のセット(主部分)
		ret.setManagementId(info.getManagementId());

		ret.setAbnormalPriorityCheck(info.getAbnormalPriorityCheck());
		ret.setAbnormalPriorityRun(info.getAbnormalPriorityRun());
		ret.setDescription(ifNull2Empty(info.getDescription()));
		ret.setOwnerRoleId(ifNull2Empty(info.getOwnerRoleId()));
		if(ifNull2Empty(info.getFacilityId()).isEmpty())
			ret.setFacilityId(SystemParameterConstant.getParamText(SystemParameterConstant.FACILITY_ID));
		else
			ret.setFacilityId(ifNull2Empty(info.getFacilityId()));
		ret.setName(ifNull2Empty(info.getName()));
		ret.setNormalPriorityCheck(info.getNormalPriorityCheck());
		ret.setNormalPriorityRun(info.getNormalPriorityRun());
		
		ret.setRegDate(DateUtil.convEpoch2DateString(info.getRegDate()));
		ret.setRegUser(ifNull2Empty(info.getRegUser()));
		ret.setUpdateDate(DateUtil.convEpoch2DateString(info.getUpdateDate()));
		ret.setUpdateUser(ifNull2Empty(info.getUpdateUser()));
		
		ret.setStartPriority(info.getStartPriority());
		ret.setScope(ifNull2Empty(info.getScope()));
		ret.setValidFlg(info.isValidFlg());
		
		List<NotifyId> notifyIds = new ArrayList<>();
		NotifyId notifyId = null;
		for(com.clustercontrol.ws.notify.NotifyRelationInfo relInfo: info.getNotifyRelationList()){
			notifyId = new NotifyId();
			notifyId.setNotifyGroupId(relInfo.getNotifyGroupId());
			notifyId.setNotifyId(relInfo.getNotifyId());
			notifyId.setNotifyType(relInfo.getNotifyType());
			notifyIds.add(notifyId);
		}
		ret.setNotifyId(notifyIds.toArray(new NotifyId[0]));
		
		List<InfraModuleInfo> modules = new ArrayList<>();
		InfraModuleInfo module = null;
		int orderNo = 0;
		for(com.clustercontrol.ws.infra.InfraModuleInfo modInfo: info.getModuleList()){
			module = new InfraModuleInfo();
			module.setModuleId(modInfo.getModuleId());
			module.setName(ifNull2Empty(modInfo.getName()));
			module.setOrderNo(++orderNo);
			module.setValidFlg(modInfo.isValidFlg());

			if(modInfo instanceof FileTransferModuleInfo){
				module.setModuleType(FileTransferModule);
				FileTransferModuleInfo filInfo = (FileTransferModuleInfo)modInfo;
				module.setDestAttribute(ifNull2Empty(filInfo.getDestAttribute()));
				module.setDestOwner(ifNull2Empty(filInfo.getDestOwner()));
				module.setDestPath(ifNull2Empty(filInfo.getDestPath()));
				module.setFileId(ifNull2Empty(filInfo.getFileId()));
				module.setSendMethodType(filInfo.getSendMethodType());
				module.setBackupIfExistFlg(filInfo.isBackupIfExistFlg());
				module.setPrecheckFileFlg(filInfo.isPrecheckFlg());
				module.setStopIfFailFileFlg(filInfo.isStopIfFailFlg());
				
				List<FileTransferVariable> variables = new ArrayList<>();
				FileTransferVariable variable = null;
				for(com.clustercontrol.ws.infra.FileTransferVariableInfo valInfo: filInfo.getFileTransferVariableList()){
					variable = new FileTransferVariable();
					variable.setName(valInfo.getName());
					variable.setValue(valInfo.getValue());
					variables.add(variable);
				}
				module.setExecReturnParamName(filInfo.getExecReturnParamName());
				module.setFileTransferVariable(variables.toArray(new FileTransferVariable[0]));
			} else
			if(modInfo instanceof CommandModuleInfo){
				module.setModuleType(ExecModule);
				CommandModuleInfo comInfo = (CommandModuleInfo)modInfo;
				module.setAccessMethodType(comInfo.getAccessMethodType());
				module.setCheckCommand(comInfo.getCheckCommand());
				module.setExecCommand(comInfo.getExecCommand());
				module.setPrecheckCommandFlg(comInfo.isPrecheckFlg());
				module.setStopIfFailCommandFlg(comInfo.isStopIfFailFlg());
				module.setExecReturnParamName(comInfo.getExecReturnParamName());
			} else if (modInfo instanceof ReferManagementModuleInfo) {
				module.setModuleType(ReferModule);
				ReferManagementModuleInfo refInfo = (ReferManagementModuleInfo) modInfo;
				module.setReferManagementId(refInfo.getReferManagementId());
			}
			modules.add(module);
		}
		ret.setInfraModuleInfo(modules.toArray(new InfraModuleInfo[0]));
		
		//環境変数
		List<InfraManagementParam> params = new ArrayList<>();
		for (InfraManagementParamInfo paramInfo : info.getInfraManagementParamList()) {
			InfraManagementParam param = new InfraManagementParam();
			param.setParamId(paramInfo.getParamId());
			param.setDescription(paramInfo.getDescription());
			param.setPasswordFlg(paramInfo.isPasswordFlg());
			param.setValue(paramInfo.getValue());
			params.add(param);
		}
		ret.setInfraManagementParam(params.toArray(new InfraManagementParam[0]));
		
		return ret;
	}

	public com.clustercontrol.ws.infra.InfraManagementInfo getDTO(InfraManagementInfo info) throws Exception {
		com.clustercontrol.ws.infra.InfraManagementInfo ret = new com.clustercontrol.ws.infra.InfraManagementInfo();

		//情報のセット(主部分)
		ret.setManagementId(info.getManagementId());
		
		ret.setAbnormalPriorityCheck(info.getAbnormalPriorityCheck());
		ret.setAbnormalPriorityRun(info.getAbnormalPriorityRun());
		ret.setDescription(ifNull2Empty(info.getDescription()));
		ret.setOwnerRoleId(ifNull2Empty(info.getOwnerRoleId()));
		if(!SystemParameterConstant.isParam(
				ifNull2Empty(info.getFacilityId()),
				SystemParameterConstant.FACILITY_ID))
			ret.setFacilityId(info.getFacilityId());
		
		ret.setName(ifNull2Empty(info.getName()));
		ret.setNormalPriorityCheck(info.getNormalPriorityCheck());
		ret.setNormalPriorityRun(info.getNormalPriorityRun());
		
		ret.setRegDate(DateUtil.convDateString2Epoch(info.getRegDate()));
		ret.setRegUser(ifNull2Empty(info.getRegUser()));
		ret.setUpdateDate(DateUtil.convDateString2Epoch(info.getUpdateDate()));
		ret.setUpdateUser(ifNull2Empty(info.getUpdateUser()));
		
		ret.setStartPriority(info.getStartPriority());
		ret.setScope(ifNull2Empty(info.getScope()));
		ret.setValidFlg(info.getValidFlg());
		
		List<com.clustercontrol.ws.notify.NotifyRelationInfo> notifyIds = new ArrayList<>();
		com.clustercontrol.ws.notify.NotifyRelationInfo notifyId = null;
		for(NotifyId relInfo: info.getNotifyId()){
			notifyId = new com.clustercontrol.ws.notify.NotifyRelationInfo();
			notifyId.setNotifyGroupId(relInfo.getNotifyGroupId());
			notifyId.setNotifyId(relInfo.getNotifyId());
			notifyId.setNotifyType(relInfo.getNotifyType());
			notifyIds.add(notifyId);
		}
		ret.getNotifyRelationList().clear();
		ret.getNotifyRelationList().addAll(notifyIds);
		
		List<com.clustercontrol.ws.infra.InfraModuleInfo> modules = new ArrayList<>();
		com.clustercontrol.ws.infra.InfraModuleInfo module = null;
		
		InfraModuleInfo[] moduleInfos = info.getInfraModuleInfo();
		sort(moduleInfos);
		for(InfraModuleInfo modInfo: moduleInfos){
			if(modInfo.getModuleType().equals(ExecModule)){
				module = new CommandModuleInfo();
				CommandModuleInfo comInfo = (CommandModuleInfo) module;
				comInfo.setAccessMethodType(modInfo.getAccessMethodType());
				comInfo.setCheckCommand(modInfo.getCheckCommand());
				comInfo.setExecCommand(modInfo.getExecCommand());
				comInfo.setPrecheckFlg(modInfo.getPrecheckCommandFlg());
				comInfo.setStopIfFailFlg(modInfo.getStopIfFailCommandFlg());
				comInfo.setExecReturnParamName(modInfo.getExecReturnParamName());
			} else
			if(modInfo.getModuleType().equals(FileTransferModule)){
				module = new FileTransferModuleInfo();
				FileTransferModuleInfo filInfo = (FileTransferModuleInfo)module;
				filInfo.setDestAttribute(ifNull2Empty(modInfo.getDestAttribute()));
				filInfo.setDestOwner(ifNull2Empty(modInfo.getDestOwner()));
				filInfo.setDestPath(ifNull2Empty(modInfo.getDestPath()));
				filInfo.setFileId(ifNull2Empty(modInfo.getFileId()));
				filInfo.setSendMethodType(modInfo.getSendMethodType());
				filInfo.setBackupIfExistFlg(modInfo.getBackupIfExistFlg());
				filInfo.setPrecheckFlg(modInfo.getPrecheckFileFlg());
				filInfo.setStopIfFailFlg(modInfo.getStopIfFailFileFlg());
				
				List<com.clustercontrol.ws.infra.FileTransferVariableInfo> variables = new ArrayList<>();
				com.clustercontrol.ws.infra.FileTransferVariableInfo variable = null;
				for(FileTransferVariable valInfo: modInfo.getFileTransferVariable()){
					variable = new com.clustercontrol.ws.infra.FileTransferVariableInfo();
					variable.setName(valInfo.getName());
					variable.setValue(valInfo.getValue());
					variables.add(variable);
				}
				filInfo.getFileTransferVariableList().clear();
				filInfo.getFileTransferVariableList().addAll(variables);
				filInfo.setExecReturnParamName(modInfo.getExecReturnParamName());
			} else if (modInfo.getModuleType().equals(ReferModule)) {
				module = new ReferManagementModuleInfo();
				ReferManagementModuleInfo refInfo = (ReferManagementModuleInfo) module;
				refInfo.setReferManagementId(modInfo.getReferManagementId());
			}
			
			module.setModuleId(modInfo.getModuleId());
			module.setName(ifNull2Empty(modInfo.getName()));
			module.setValidFlg(modInfo.getValidFlg());
			module.setOrderNo(modInfo.getOrderNo());

			modules.add(module);
		}
		ret.getModuleList().clear();
		ret.getModuleList().addAll(modules);
		
		if (info.getInfraManagementParam() != null) {
			for (InfraManagementParam param : info.getInfraManagementParam()) {
				InfraManagementParamInfo paramInfo = new InfraManagementParamInfo();
				paramInfo.setParamId(param.getParamId());
				paramInfo.setDescription(param.getDescription());
				paramInfo.setPasswordFlg(param.getPasswordFlg());
				paramInfo.setValue(param.getValue());
				ret.getInfraManagementParamList().add(paramInfo);
			}
		}
		return ret;
	}
	
	private static void sort(InfraModuleInfo[] objects) {
		Arrays.sort(
			objects,
			new Comparator<InfraModuleInfo>() {
				@Override
				public int compare(InfraModuleInfo obj1, InfraModuleInfo obj2) {
					return obj1.getOrderNo() - obj2.getOrderNo();
				}
			});
	}
}
