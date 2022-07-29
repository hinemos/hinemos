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

import org.openapitools.client.model.CommandModuleInfoResponse;
import org.openapitools.client.model.FileTransferModuleInfoResponse;
import org.openapitools.client.model.FileTransferVariableInfoResponse;
import org.openapitools.client.model.InfraManagementInfoResponse;
import org.openapitools.client.model.InfraManagementParamInfoResponse;
import org.openapitools.client.model.NotifyRelationInfoResponse;
import org.openapitools.client.model.ReferManagementModuleInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.jobmanagement.bean.SystemParameterConstant;
import com.clustercontrol.utility.settings.infra.xml.FileTransferVariable;
import com.clustercontrol.utility.settings.infra.xml.InfraManagementInfo;
import com.clustercontrol.utility.settings.infra.xml.InfraManagementParam;
import com.clustercontrol.utility.settings.infra.xml.InfraModuleInfo;
import com.clustercontrol.utility.settings.infra.xml.NotifyId;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.util.OpenApiEnumConverter;

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
	public InfraManagementInfo getXmlInfo(InfraManagementInfoResponse info) throws Exception {

		InfraManagementInfo ret = new InfraManagementInfo();

		//情報のセット(主部分)
		ret.setManagementId(info.getManagementId());

		ret.setAbnormalPriorityCheck(OpenApiEnumConverter.enumToInteger(info.getAbnormalPriorityCheck()));
		ret.setAbnormalPriorityRun(OpenApiEnumConverter.enumToInteger(info.getAbnormalPriorityRun()));
		ret.setDescription(ifNull2Empty(info.getDescription()));
		ret.setOwnerRoleId(ifNull2Empty(info.getOwnerRoleId()));
		if(ifNull2Empty(info.getFacilityId()).isEmpty())
			ret.setFacilityId(SystemParameterConstant.getParamText(SystemParameterConstant.FACILITY_ID));
		else
			ret.setFacilityId(ifNull2Empty(info.getFacilityId()));
		ret.setName(ifNull2Empty(info.getName()));
		ret.setNormalPriorityCheck(OpenApiEnumConverter.enumToInteger(info.getNormalPriorityCheck()));
		ret.setNormalPriorityRun(OpenApiEnumConverter.enumToInteger(info.getNormalPriorityRun()));
		
		ret.setRegDate(info.getRegDate());
		ret.setRegUser(ifNull2Empty(info.getRegUser()));
		ret.setUpdateDate(info.getUpdateDate());
		ret.setUpdateUser(ifNull2Empty(info.getUpdateUser()));
		
		ret.setStartPriority(OpenApiEnumConverter.enumToInteger(info.getStartPriority()));
		ret.setScope(ifNull2Empty(info.getScope()));
		ret.setValidFlg(info.getValidFlg());
		
		List<NotifyId> notifyIds = new ArrayList<>();
		NotifyId notifyId = null;
		for(NotifyRelationInfoResponse relInfo: info.getNotifyRelationList()){
			notifyId = new NotifyId();
			notifyId.setNotifyId(relInfo.getNotifyId());
			notifyId.setNotifyType(OpenApiEnumConverter.enumToInteger(relInfo.getNotifyType()));
			notifyIds.add(notifyId);
		}
		ret.setNotifyId(notifyIds.toArray(new NotifyId[0]));
		
		List<InfraModuleInfo> modules = new ArrayList<>();
		InfraModuleInfo module = null;
		
		//ファイル転送
		for(FileTransferModuleInfoResponse modInfo: info.getFileTransferModuleInfoList()){
			module = new InfraModuleInfo();
			module.setModuleId(modInfo.getModuleId());
			module.setName(ifNull2Empty(modInfo.getName()));
			module.setOrderNo(modInfo.getOrderNo());
			module.setValidFlg(modInfo.getValidFlg());

			module.setModuleType(FileTransferModule);
			module.setDestAttribute(ifNull2Empty(modInfo.getDestAttribute()));
			module.setDestOwner(ifNull2Empty(modInfo.getDestOwner()));
			module.setDestPath(ifNull2Empty(modInfo.getDestPath()));
			module.setFileId(ifNull2Empty(modInfo.getFileId()));
			module.setSendMethodType(OpenApiEnumConverter.enumToInteger(modInfo.getSendMethodType()));
			module.setBackupIfExistFlg(modInfo.getBackupIfExistFlg());
			module.setPrecheckFileFlg(modInfo.getPrecheckFlg());
			module.setStopIfFailFileFlg(modInfo.getStopIfFailFlg());
			
			List<FileTransferVariable> variables = new ArrayList<>();
			FileTransferVariable variable = null;
			for(FileTransferVariableInfoResponse valInfo: modInfo.getFileTransferVariableInfoEntities() ){
				variable = new FileTransferVariable();
				variable.setName(valInfo.getName());
				variable.setValue(valInfo.getValue());
				variables.add(variable);
			}
			module.setExecReturnParamName(modInfo.getExecReturnParamName());
			module.setFileTransferVariable(variables.toArray(new FileTransferVariable[0]));

			modules.add(module);
		}
		
		//コマンド
		for(CommandModuleInfoResponse modInfo: info.getCommandModuleInfoList()){
			module = new InfraModuleInfo();
			module.setModuleId(modInfo.getModuleId());
			module.setName(ifNull2Empty(modInfo.getName()));
			module.setOrderNo(modInfo.getOrderNo());
			module.setValidFlg(modInfo.getValidFlg());

			module.setModuleType(ExecModule);
			module.setAccessMethodType(OpenApiEnumConverter.enumToInteger(modInfo.getAccessMethodType()));
			module.setCheckCommand(modInfo.getCheckCommand());
			module.setExecCommand(modInfo.getExecCommand());
			module.setPrecheckCommandFlg(modInfo.getPrecheckFlg());
			module.setStopIfFailCommandFlg(modInfo.getStopIfFailFlg());
			module.setExecReturnParamName(modInfo.getExecReturnParamName());

			modules.add(module);
		}
		
		//参照
		for(ReferManagementModuleInfoResponse modInfo: info.getReferManagementModuleInfoList()){
			module = new InfraModuleInfo();
			module.setModuleId(modInfo.getModuleId());
			module.setName(ifNull2Empty(modInfo.getName()));
			module.setOrderNo(modInfo.getOrderNo());
			module.setValidFlg(modInfo.getValidFlg());

			module.setModuleType(ReferModule);
			module.setReferManagementId(modInfo.getReferManagementId());

			modules.add(module);
		}
		//モジュールの順番号を調整（DB上は0スタートの場合もあるので、無条件に1スタートに変更）
		InfraModuleInfo[] moduleArray = modules.toArray(new InfraModuleInfo[0]);
		sort(moduleArray);
		for (int recCount = 0; recCount < moduleArray.length; recCount++) {
			moduleArray[recCount].setOrderNo(recCount + 1);
		}
		
		ret.setInfraModuleInfo(moduleArray);
		
		//環境変数
		List<InfraManagementParam> params = new ArrayList<>();
		for (InfraManagementParamInfoResponse paramInfo : info.getInfraManagementParamInfoEntities() ) {
			InfraManagementParam param = new InfraManagementParam();
			param.setParamId(paramInfo.getParamId());
			param.setDescription(paramInfo.getDescription());
			param.setPasswordFlg(paramInfo.getPasswordFlg());
			param.setValue(paramInfo.getValue());
			params.add(param);
		}
		ret.setInfraManagementParam(params.toArray(new InfraManagementParam[0]));
		
		return ret;
	}

	public InfraManagementInfoResponse getDTO(InfraManagementInfo info) throws InvalidSetting, HinemosUnknown {
		InfraManagementInfoResponse ret = new InfraManagementInfoResponse();

		//情報のセット(主部分)
		ret.setManagementId(info.getManagementId());
		
		ret.setAbnormalPriorityCheck(OpenApiEnumConverter.integerToEnum(info.getAbnormalPriorityCheck(), InfraManagementInfoResponse.AbnormalPriorityCheckEnum.class));
		ret.setAbnormalPriorityRun(OpenApiEnumConverter.integerToEnum(info.getAbnormalPriorityRun() , InfraManagementInfoResponse.AbnormalPriorityRunEnum.class));
		ret.setDescription(ifNull2Empty(info.getDescription()));
		ret.setOwnerRoleId(ifNull2Empty(info.getOwnerRoleId()));
		if(!SystemParameterConstant.isParam(
				ifNull2Empty(info.getFacilityId()),
				SystemParameterConstant.FACILITY_ID))
			ret.setFacilityId(info.getFacilityId());
		
		ret.setName(ifNull2Empty(info.getName()));
		ret.setNormalPriorityCheck(OpenApiEnumConverter.integerToEnum(info.getNormalPriorityCheck(), InfraManagementInfoResponse.NormalPriorityCheckEnum.class));
		ret.setNormalPriorityRun(OpenApiEnumConverter.integerToEnum(info.getNormalPriorityRun(), InfraManagementInfoResponse.NormalPriorityRunEnum.class));
		
		ret.setRegDate(info.getRegDate());
		ret.setRegUser(ifNull2Empty(info.getRegUser()));
		ret.setUpdateDate(info.getUpdateDate());
		ret.setUpdateUser(ifNull2Empty(info.getUpdateUser()));
		
		ret.setStartPriority(OpenApiEnumConverter.integerToEnum(info.getStartPriority(), InfraManagementInfoResponse.StartPriorityEnum.class));
		ret.setScope(ifNull2Empty(info.getScope()));
		ret.setValidFlg(info.getValidFlg());
		
		List<NotifyRelationInfoResponse> notifyIds = new ArrayList<>();
		NotifyRelationInfoResponse notifyId = null;
		for(NotifyId relInfo: info.getNotifyId()){
			notifyId = new NotifyRelationInfoResponse();
			notifyId.setNotifyId(relInfo.getNotifyId());
			notifyId.setNotifyType(OpenApiEnumConverter.integerToEnum((int)relInfo.getNotifyType(),NotifyRelationInfoResponse.NotifyTypeEnum.class));
			notifyIds.add(notifyId);
		}
		ret.getNotifyRelationList().clear();
		ret.getNotifyRelationList().addAll(notifyIds);
		
		List<FileTransferModuleInfoResponse> fileModules = new ArrayList<>();
		List<CommandModuleInfoResponse> commandModules = new ArrayList<>();
		List<ReferManagementModuleInfoResponse> referModules = new ArrayList<>();
		
		InfraModuleInfo[] moduleInfos = info.getInfraModuleInfo();
		sort(moduleInfos);
		for(InfraModuleInfo modInfo: moduleInfos){
			if(modInfo.getModuleType().equals(ExecModule)){
				CommandModuleInfoResponse module = new CommandModuleInfoResponse();
				module.setAccessMethodType(OpenApiEnumConverter.integerToEnum(modInfo.getAccessMethodType(),CommandModuleInfoResponse.AccessMethodTypeEnum.class));
				module.setCheckCommand(modInfo.getCheckCommand());
				module.setExecCommand(modInfo.getExecCommand());
				module.setPrecheckFlg(modInfo.getPrecheckCommandFlg());
				module.setStopIfFailFlg(modInfo.getStopIfFailCommandFlg());
				module.setExecReturnParamName(modInfo.getExecReturnParamName());
				module.setModuleId(modInfo.getModuleId());
				module.setName(ifNull2Empty(modInfo.getName()));
				module.setValidFlg(modInfo.getValidFlg());
				module.setOrderNo(modInfo.getOrderNo());

				commandModules.add(module);
			} else
			if(modInfo.getModuleType().equals(FileTransferModule)){
				FileTransferModuleInfoResponse module = new FileTransferModuleInfoResponse();
				module.setDestAttribute(ifEmpty2Null(modInfo.getDestAttribute()));
				module.setDestOwner(ifEmpty2Null(modInfo.getDestOwner()));
				module.setDestPath(ifNull2Empty(modInfo.getDestPath()));
				module.setFileId(ifNull2Empty(modInfo.getFileId()));
				module.setSendMethodType(OpenApiEnumConverter.integerToEnum(modInfo.getSendMethodType(),FileTransferModuleInfoResponse.SendMethodTypeEnum.class));
				module.setBackupIfExistFlg(modInfo.getBackupIfExistFlg());
				module.setPrecheckFlg(modInfo.getPrecheckFileFlg());
				module.setStopIfFailFlg(modInfo.getStopIfFailFileFlg());
				
				List<FileTransferVariableInfoResponse> variables = new ArrayList<>();
				for(FileTransferVariable valInfo: modInfo.getFileTransferVariable()){
					FileTransferVariableInfoResponse variable = new FileTransferVariableInfoResponse();
					variable.setName(valInfo.getName());
					variable.setValue(valInfo.getValue());
					variables.add(variable);
				}
				module.setFileTransferVariableInfoEntities(variables);
				module.setExecReturnParamName(modInfo.getExecReturnParamName());
				module.setModuleId(modInfo.getModuleId());
				module.setName(ifNull2Empty(modInfo.getName()));
				module.setValidFlg(modInfo.getValidFlg());
				module.setOrderNo(modInfo.getOrderNo());

				fileModules.add(module);
			} else if (modInfo.getModuleType().equals(ReferModule)) {
				ReferManagementModuleInfoResponse module = new ReferManagementModuleInfoResponse();
				module.setReferManagementId(modInfo.getReferManagementId());
				module.setModuleId(modInfo.getModuleId());
				module.setName(ifNull2Empty(modInfo.getName()));
				module.setValidFlg(modInfo.getValidFlg());
				module.setOrderNo(modInfo.getOrderNo());

				referModules.add(module);
			}
			
		}
		ret.setFileTransferModuleInfoList(fileModules);
		ret.setCommandModuleInfoList(commandModules);
		ret.setReferManagementModuleInfoList(referModules);
		
		if (info.getInfraManagementParam() != null) {
			List<InfraManagementParamInfoResponse> paramList = new ArrayList<InfraManagementParamInfoResponse>();
			for (InfraManagementParam param : info.getInfraManagementParam()) {
				InfraManagementParamInfoResponse paramInfo = new InfraManagementParamInfoResponse();
				paramInfo.setParamId(param.getParamId());
				paramInfo.setDescription(param.getDescription());
				paramInfo.setPasswordFlg(param.getPasswordFlg());
				paramInfo.setValue(param.getValue());
				paramList.add(paramInfo);
			}
			ret.setInfraManagementParamInfoEntities(paramList);
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
