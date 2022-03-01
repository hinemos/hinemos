/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

import com.clustercontrol.commons.util.CommonValidator;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;
import com.clustercontrol.xcloud.bean.AutoAssignNodePatternEntryType;
import com.clustercontrol.xcloud.common.ErrorCode;
import com.clustercontrol.xcloud.util.Cidr;

public class AutoAssignNodePatternEntryInfoRequest implements RequestDto {
	@RestItemName(MessageConstant.SCOPE)
	@RestValidateString(type = CheckType.ID)
	private String scopeId;
	
	@RestItemName(MessageConstant.XCLOUD_CORE_AUTO_ASSIGN_NODE_PATTERN_TYPE)
	@RestValidateObject(notNull = true)
	private AutoAssignNodePatternEntryType patternType;
	
	@RestItemName(MessageConstant.XCLOUD_CORE_AUTO_ASSIGN_NODE_PATTERN)
	@RestValidateString(notNull = true, minLen=1, maxLen=256)
	private String pattern;
	
	public AutoAssignNodePatternEntryInfoRequest() {
	}
	
	@Override
	public void correlationCheck() throws InvalidSetting {
		switch (getPatternType()) {
		case cidr:
			if (!Cidr.cidrPattern.matcher(getPattern()).matches())
				throw new InvalidSetting(ErrorCode.AUTO_ASSIGN_NODE_UNMATCHED_CIDR_FORMAT.cloudManagerFault(getPattern()));
			break;
		case instanceName:
			try {
				CommonValidator.validateRegex("", getPattern(), true);
			} catch (InvalidSetting e) {
				throw new InvalidSetting(ErrorCode.AUTO_ASSIGN_NODE_UNMATCHED_REGEX.cloudManagerFault(getPattern()));
			}
			break;
		}
	}
	
	public String getScopeId() {
		return scopeId;
	}
	public void setScopeId(String scopeId) {
		this.scopeId = scopeId;
	}
	public AutoAssignNodePatternEntryType getPatternType() {
		return patternType;
	}
	public void setPatternType(AutoAssignNodePatternEntryType patternType) {
		this.patternType = patternType;
	}
	public String getPattern() {
		return pattern;
	}
	public void setPattern(String pattern) {
		this.pattern = pattern;
	}
	
}
