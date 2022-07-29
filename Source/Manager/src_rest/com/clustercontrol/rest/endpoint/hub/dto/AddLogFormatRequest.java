/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.hub.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class AddLogFormatRequest implements RequestDto {
	@RestItemName(value = MessageConstant.HUB_LOG_FORMAT_ID)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 64, type = CheckType.ID)
	private String logFormatId;

	@RestItemName(value = MessageConstant.DESCRIPTION)
	@RestValidateString(maxLen = 256)
	private String description;

	@RestItemName(value = MessageConstant.HUB_LOG_FORMAT_TIMESTAMP_REGEX)
	@RestValidateString(notNull = false)
	private String timestampRegex;

	@RestItemName(value = MessageConstant.HUB_LOG_FORMAT_TIMESTAMP_FORMAT)
	@RestValidateString(notNull = false)
	private String timestampFormat;

	@RestItemName(value = MessageConstant.HUB_LOG_FORMAT_KEYS)
	private List<LogFormatKeyRequest> keyPatternList = new ArrayList<>();

	@RestItemName(value = MessageConstant.OWNER_ROLE_ID)
	@RestValidateString(notNull = true, maxLen = 64)
	private String ownerRoleId;

	public String getLogFormatId() {
		return logFormatId;
	}
	public void setLogFormatId(String logFormatId) {
		this.logFormatId = logFormatId;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getTimestampRegex() {
		return timestampRegex;
	}
	public void setTimestampRegex(String timestampRegex) {
		this.timestampRegex = timestampRegex;
	}

	public String getTimestampFormat() {
		return timestampFormat;
	}
	public void setTimestampFormat(String timestampFormat) {
		this.timestampFormat = timestampFormat;
	}

	public List<LogFormatKeyRequest> getKeyPatternList() {
		return keyPatternList;
	}
	public void setKeyPatternList(List<LogFormatKeyRequest> keys) {
		this.keyPatternList = keys;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}
	@Override
	public void correlationCheck() throws InvalidSetting {
		if (keyPatternList != null) {
			for (LogFormatKeyRequest key : keyPatternList) {
				key.correlationCheck();
			}
		}
	}
}
