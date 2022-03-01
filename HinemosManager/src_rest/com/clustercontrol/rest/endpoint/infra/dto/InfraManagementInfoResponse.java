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

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertIgnore;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;
import com.clustercontrol.rest.endpoint.infra.dto.enumtype.PrioritySelectEnum;
import com.clustercontrol.rest.endpoint.notify.dto.NotifyRelationInfoResponse;

public class InfraManagementInfoResponse {

	private String managementId;
	private String name;
	private String description;
	private String facilityId;
	private String notifyGroupId;
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
	private List<CommandModuleInfoResponse> commandModuleInfoList = new ArrayList<>();
	@RestBeanConvertIgnore
	private List<FileTransferModuleInfoResponse> fileTransferModuleInfoList = new ArrayList<>();
	@RestBeanConvertIgnore
	private List<ReferManagementModuleInfoResponse> referManagementModuleInfoList = new ArrayList<>();

	private List<InfraManagementParamInfoResponse> infraManagementParamInfoEntities = new ArrayList<>();
	@RestBeanConvertDatetime
	private String regDate;
	private String regUser;
	@RestBeanConvertDatetime
	private String updateDate;
	private String updateUser;

	private List<NotifyRelationInfoResponse> notifyRelationList;

	@RestPartiallyTransrateTarget
	private String scope;

	private String ownerRoleId;

	public InfraManagementInfoResponse() {
	}

	public InfraManagementInfoResponse(String managementId) {
		this.setManagementId(managementId);
	}

	public String getManagementId() {
		return managementId;
	}

	public void setManagementId(String managementId) {
		this.managementId = managementId;
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

	public String getNotifyGroupId() {
		return notifyGroupId;
	}

	public void setNotifyGroupId(String notifyGroupId) {
		this.notifyGroupId = notifyGroupId;
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

	public List<CommandModuleInfoResponse> getCommandModuleInfoList() {
		return commandModuleInfoList;
	}

	public void setCommandModuleInfoList(List<CommandModuleInfoResponse> commandModuleInfoList) {
		this.commandModuleInfoList = commandModuleInfoList;
	}

	public List<FileTransferModuleInfoResponse> getFileTransferModuleInfoList() {
		return fileTransferModuleInfoList;
	}

	public void setFileTransferModuleInfoList(List<FileTransferModuleInfoResponse> fileTransferModuleInfoList) {
		this.fileTransferModuleInfoList = fileTransferModuleInfoList;
	}

	public List<ReferManagementModuleInfoResponse> getReferManagementModuleInfoList() {
		return referManagementModuleInfoList;
	}

	public void setReferManagementModuleInfoList(
			List<ReferManagementModuleInfoResponse> referManagementModuleInfoList) {
		this.referManagementModuleInfoList = referManagementModuleInfoList;
	}

	public List<InfraManagementParamInfoResponse> getInfraManagementParamInfoEntities() {
		return infraManagementParamInfoEntities;
	}

	public void setInfraManagementParamInfoEntities(
			List<InfraManagementParamInfoResponse> infraManagementParamInfoEntities) {
		this.infraManagementParamInfoEntities = infraManagementParamInfoEntities;
	}

	public String getRegDate() {
		return regDate;
	}

	public void setRegDate(String regDate) {
		this.regDate = regDate;
	}

	public String getRegUser() {
		return regUser;
	}

	public void setRegUser(String regUser) {
		this.regUser = regUser;
	}

	public String getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(String updateDate) {
		this.updateDate = updateDate;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public List<NotifyRelationInfoResponse> getNotifyRelationList() {
		return notifyRelationList;
	}

	public void setNotifyRelationList(List<NotifyRelationInfoResponse> notifyRelationList) {
		this.notifyRelationList = notifyRelationList;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	@Override
	public String toString() {
		return "InfraManagementInfoResponse [managementId=" + managementId + ", name=" + name + ", description="
				+ description + ", facilityId=" + facilityId + ", notifyGroupId=" + notifyGroupId + ", validFlg="
				+ validFlg + ", startPriority=" + startPriority + ", normalPriorityRun=" + normalPriorityRun
				+ ", abnormalPriorityRun=" + abnormalPriorityRun + ", normalPriorityCheck=" + normalPriorityCheck
				+ ", abnormalPriorityCheck=" + abnormalPriorityCheck + ", commandModuleInfoList="
				+ commandModuleInfoList + ", fileTransferModuleInfoList=" + fileTransferModuleInfoList
				+ ", referManagementModuleInfoList=" + referManagementModuleInfoList
				+ ", infraManagementParamInfoEntities=" + infraManagementParamInfoEntities + ", regDate=" + regDate
				+ ", regUser=" + regUser + ", updateDate=" + updateDate + ", updateUser=" + updateUser
				+ ", notifyRelationList=" + notifyRelationList + ", scope=" + scope + ", ownerRoleId=" + ownerRoleId
				+ "]";
	}

}
