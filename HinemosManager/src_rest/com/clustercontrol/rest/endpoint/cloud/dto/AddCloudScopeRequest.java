/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class AddCloudScopeRequest implements RequestDto {
	@RestItemName(MessageConstant.XCLOUD_CORE_CLOUDPLATFORM_ID)
	@RestValidateObject(notNull = true)
	private String platformId;
	@RestItemName(MessageConstant.XCLOUD_CORE_CLOUDSCOPE_ID)
	@RestValidateString(notNull = true, minLen = 1, type = CheckType.ID)
	private String cloudScopeId;
	@RestItemName(MessageConstant.XCLOUD_CORE_CLOUDSCOPE_NAME)
	@RestValidateString(notNull = true, maxLen=256)
	private String scopeName;
	@RestItemName(MessageConstant.XCLOUD_CORE_OWNERROLE_ID)
	@RestValidateString(notNull = true, minLen = 1, type = CheckType.ID)
	private String ownerRoleId;
	@RestItemName(MessageConstant.XCLOUD_CORE_DESCRIPTION)
	@RestValidateString(maxLen=256)
	private String description;
	@RestItemName(MessageConstant.XCLOUD_CORE_CLOUDSCOPE_ACCOUNT)
	@RestValidateObject(notNull = true)
	private AddCloudLoginUserRequest account;

	@RestItemName(MessageConstant.XCLOUD_CORE_PRIVATE_LOCATIONS)
	@RestValidateObject(notNull = true)
	private List<PrivateLocationRequest> privateLocations;

	public AddCloudScopeRequest() {
	}
	
	@Override
	public void correlationCheck() throws InvalidSetting {
	}

	public String getPlatformId() {
		return platformId;
	}

	public void setPlatformId(String platformId) {
		this.platformId = platformId;
	}

	public String getCloudScopeId() {
		return cloudScopeId;
	}

	public void setCloudScopeId(String cloudScopeId) {
		this.cloudScopeId = cloudScopeId;
	}

	public String getScopeName() {
		return scopeName;
	}

	public void setScopeName(String scopeName) {
		this.scopeName = scopeName;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public AddCloudLoginUserRequest getAccount() {
		return account;
	}

	public void setAccount(AddCloudLoginUserRequest account) {
		this.account = account;
	}

	public List<PrivateLocationRequest> getPrivateLocations() {
		return privateLocations;
	}

	public void setPrivateLocations(List<PrivateLocationRequest> privateLocations) {
		this.privateLocations = privateLocations;
	}
}
