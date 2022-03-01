/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.rpa.dto;

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;
public class AddRpaScenarioTagRequest implements RequestDto {
	
	public AddRpaScenarioTagRequest (){}
	
	/** タグID */
	@RestItemName(value = MessageConstant.RPA_SCENARIO_TAG_ID)
	@RestValidateString(notNull = true, maxLen = 64, type = CheckType.ID)
	private String tagId;
	/** タグ名 */
	@RestItemName(value = MessageConstant.RPA_SCENARIO_TAG_NAME)
	@RestValidateString(notNull = true, minLen = 1)
	private String tagName;
	/** 説明 */
	@RestItemName(value = MessageConstant.DESCRIPTION)
	private String description;
	/** タグ階層パス */
	@RestItemName(value = MessageConstant.RPA_SCENARIO_TAG_PATH)
	private String tagPath;
	/** オーナーロールID */
	@RestItemName(value = MessageConstant.OWNER_ROLE_ID)
	@RestValidateString(notNull = true, maxLen = 64)
	private String ownerRoleId;

	/** タグID */
	public String getTagId() {
		return tagId;
	}
	public void setTagId(String tagId) {
		this.tagId = tagId;
	}

	/** タグ名  */
	public String getTagName() {
		return tagName;
	}
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	/** 説明 */
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}

	/** タグ階層パス */
	public String getTagPath() {
		return tagPath;
	}
	public void setTagPath(String tagPath) {
		this.tagPath = tagPath;
	}

	/** オーナーロールID */
	public String getOwnerRoleId() {
		return this.ownerRoleId;
	}
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	@Override
	public String toString() {
		return "RpaScenarioResponse [tagId=" + tagId + ", tagName=" + tagName 
				+ ", description=" + description + ", tagPath=" + tagPath + ", ownerRoleId=" + ownerRoleId
				+ "]";
	}
	
	@Override
	public void correlationCheck() throws InvalidSetting {
		CommonValidator.validateOwnerRoleId(ownerRoleId, false, tagId, HinemosModuleConstant.RPA_SCENARIO_TAG);
	}

}
