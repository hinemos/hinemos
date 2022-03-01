/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class SetStatusNodeConfigSettingRequest implements RequestDto {

	private List<String> settingId = new ArrayList<>();
	private Boolean validFlag;

	public SetStatusNodeConfigSettingRequest() {
	}

	public List<String> getSettingId() {
		return settingId;
	}

	public void setSettingId(List<String> settingId) {
		this.settingId = settingId;
	}

	public Boolean isValidFlag() {
		return validFlag;
	}

	public void setValidFlag(Boolean validFlag) {
		this.validFlag = validFlag;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
