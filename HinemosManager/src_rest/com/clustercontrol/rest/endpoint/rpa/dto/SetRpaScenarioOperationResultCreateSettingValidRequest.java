/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.rpa.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class SetRpaScenarioOperationResultCreateSettingValidRequest implements RequestDto {
	@RestItemName(value = MessageConstant.RPA_SCENARIO_OPERATION_RESULT_CREATE_SETTING_ID)
	private List<String> settingIdList = new ArrayList<>();

	@RestItemName(value = MessageConstant.SETTING_VALID)
	@RestValidateObject(notNull = true)
	private Boolean validFlg;

	public List<String> getSettingIdList() {
		return settingIdList;
	}
	public void setSettingIdList(List<String> settingIdList) {
		this.settingIdList = settingIdList;
	}
	public Boolean getValidFlg() {
		return validFlg;
	}
	public void setValidFlg(Boolean validFlg) {
		this.validFlg = validFlg;
	}

	/* (non-Javadoc)
	 * @see com.clustercontrol.rest.dto.RequestDto#correlationCheck()
	 */
	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
