/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.infra.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.endpoint.infra.dto.enumtype.PrioritySelectEnum;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIgnore;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.notify.dto.NotifyRelationInfoRequest;
import com.clustercontrol.util.MessageConstant;

public class ModifyInfraManagementRequest implements RequestDto {
	
	@RestItemName(value = MessageConstant.INFRA_MANAGEMENT_NAME)
	@RestValidateString(maxLen = 64, minLen = 1)
	private String name;
	
	@RestItemName(value = MessageConstant.INFRA_MANAGEMENT_DESCRIPTION)
	@RestValidateString(maxLen = 1024, minLen = 0)
	private String description;
	
	@RestItemName(value = MessageConstant.FACILITY_ID)
	@RestValidateString(maxLen = 512, minLen = 1)
	private String facilityId;
	
	@RestItemName(value = MessageConstant.VALID_FLG)
	private Boolean validFlg;
	
	@RestBeanConvertEnum
	private PrioritySelectEnum startPriority;
	
	@RestBeanConvertEnum
	private PrioritySelectEnum normalPriorityRun;
	
	@RestBeanConvertEnum
	private PrioritySelectEnum abnormalPriorityRun;
	
	@RestBeanConvertEnum
	private PrioritySelectEnum normalPriorityCheck;
	
	@RestBeanConvertEnum
	private PrioritySelectEnum abnormalPriorityCheck;
	
	@RestBeanConvertIgnore
	private List<CommandModuleInfoRequest> commandModuleInfoList = new ArrayList<>();
	
	@RestBeanConvertIgnore
	private List<FileTransferModuleInfoRequest> fileTransferModuleInfoList = new ArrayList<>();
	
	@RestBeanConvertIgnore
	private List<ReferManagementModuleInfoRequest> referManagementModuleInfoList = new ArrayList<>();

	private List<InfraManagementParamInfoRequest> infraManagementParamInfoEntities = new ArrayList<>();

	private List<NotifyRelationInfoRequest> notifyRelationList;

	public ModifyInfraManagementRequest() {
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public Boolean getValidFlg() {
		return validFlg;
	}

	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}

	public PrioritySelectEnum getStartPriority() {
		return startPriority;
	}

	public void setStartPriority(PrioritySelectEnum startPriority) {
		this.startPriority = startPriority;
	}

	public PrioritySelectEnum getNormalPriorityRun() {
		return normalPriorityRun;
	}

	public void setNormalPriorityRun(PrioritySelectEnum normalPriorityRun) {
		this.normalPriorityRun = normalPriorityRun;
	}

	public PrioritySelectEnum getAbnormalPriorityRun() {
		return abnormalPriorityRun;
	}

	public void setAbnormalPriorityRun(PrioritySelectEnum abnormalPriorityRun) {
		this.abnormalPriorityRun = abnormalPriorityRun;
	}

	public PrioritySelectEnum getNormalPriorityCheck() {
		return normalPriorityCheck;
	}

	public void setNormalPriorityCheck(PrioritySelectEnum normalPriorityCheck) {
		this.normalPriorityCheck = normalPriorityCheck;
	}

	public PrioritySelectEnum getAbnormalPriorityCheck() {
		return abnormalPriorityCheck;
	}

	public void setAbnormalPriorityCheck(PrioritySelectEnum abnormalPriorityCheck) {
		this.abnormalPriorityCheck = abnormalPriorityCheck;
	}

	public List<CommandModuleInfoRequest> getCommandModuleInfoList() {
		return commandModuleInfoList;
	}

	public void setCommandModuleInfoList(List<CommandModuleInfoRequest> commandModuleInfoList) {
		this.commandModuleInfoList = commandModuleInfoList;
	}

	public List<FileTransferModuleInfoRequest> getFileTransferModuleInfoList() {
		return fileTransferModuleInfoList;
	}

	public void setFileTransferModuleInfoList(List<FileTransferModuleInfoRequest> fileTransferModuleInfoList) {
		this.fileTransferModuleInfoList = fileTransferModuleInfoList;
	}

	public List<ReferManagementModuleInfoRequest> getReferManagementModuleInfoList() {
		return referManagementModuleInfoList;
	}

	public void setReferManagementModuleInfoList(List<ReferManagementModuleInfoRequest> referManagementModuleInfoList) {
		this.referManagementModuleInfoList = referManagementModuleInfoList;
	}

	public List<InfraManagementParamInfoRequest> getInfraManagementParamInfoEntities() {
		return infraManagementParamInfoEntities;
	}

	public void setInfraManagementParamInfoEntities(
			List<InfraManagementParamInfoRequest> infraManagementParamInfoEntities) {
		this.infraManagementParamInfoEntities = infraManagementParamInfoEntities;
	}

	public List<NotifyRelationInfoRequest> getNotifyRelationList() {
		return notifyRelationList;
	}

	public void setNotifyRelationList(List<NotifyRelationInfoRequest> notifyRelationList) {
		this.notifyRelationList = notifyRelationList;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

	@Override
	public String toString() {
		return "ModifyInfraManagementRequest [name=" + name + ", description=" + description + ", facilityId="
				+ facilityId + ", validFlg=" + validFlg + ", startPriority=" + startPriority + ", normalPriorityRun="
				+ normalPriorityRun + ", abnormalPriorityRun=" + abnormalPriorityRun + ", normalPriorityCheck="
				+ normalPriorityCheck + ", abnormalPriorityCheck=" + abnormalPriorityCheck + ", commandModuleInfoList="
				+ commandModuleInfoList + ", fileTransferModuleInfoList=" + fileTransferModuleInfoList
				+ ", referManagementModuleInfoList=" + referManagementModuleInfoList
				+ ", infraManagementParamInfoEntities=" + infraManagementParamInfoEntities + ", notifyRelationList="
				+ notifyRelationList + "]";
	}

}
