/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.sdml.dto;

import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateCollection;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class SetSdmlControlSettingStatusRequest implements RequestDto {
	@RestItemName(value = MessageConstant.APPLICATION_ID)
	@RestValidateCollection(notNull = true, minSize = 1)
	private List<String> applicationIds;

	@RestItemName(value = MessageConstant.VALID_FLG)
	@RestValidateObject(notNull = true)
	private Boolean validFlg;

	public SetSdmlControlSettingStatusRequest() {
	}

	public List<String> getApplicationIds() {
		return applicationIds;
	}

	public void setApplicationIds(List<String> applicationIds) {
		this.applicationIds = applicationIds;
	}

	public Boolean getValidFlg() {
		return validFlg;
	}

	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
