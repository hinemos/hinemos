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
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class SnapshotInstanceRequest implements RequestDto {
	@RestItemName(MessageConstant.XCLOUD_CORE_INSTANCESNAPSHOT_NAME)
	@RestValidateString(notNull = true, maxLen = 128)
	private String name;
	
	@RestItemName(MessageConstant.XCLOUD_CORE_DESCRIPTION)
	@RestValidateString(maxLen = 256)
	private String description;

	@RestItemName(MessageConstant.XCLOUD_CORE_INSTANCE_ID)
	@RestValidateString(notNull = true, minLen = 1, maxLen = 64, type = CheckType.ID)
	private String instanceId;

	private List<OptionRequest> options;

	public SnapshotInstanceRequest() {
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
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
	public String getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public List<OptionRequest> getOptions() {
		return options;
	}

	public void setOptions(List<OptionRequest> options) {
		this.options = options;
	}
}
