/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.rpa.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class ModifyRpaScenarioTagRequest implements RequestDto {
	
	public ModifyRpaScenarioTagRequest (){}
	
	/** タグ名 */
	@RestItemName(value = MessageConstant.RPA_SCENARIO_TAG_NAME)
	@RestValidateString(notNull = true, minLen=1)
	private String tagName;
	/** 説明 */
	@RestItemName(value = MessageConstant.DESCRIPTION)
	private String description;


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

	@Override
	public String toString() {
		return "RpaScenarioResponse [tagName=" + tagName + ", description=" + description
				+ "]";
	}
	
	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
