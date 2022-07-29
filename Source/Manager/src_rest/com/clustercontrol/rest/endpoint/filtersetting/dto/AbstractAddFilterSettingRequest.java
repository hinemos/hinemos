/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.filtersetting.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.filtersetting.bean.FilterSettingConstant;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateString;
import com.clustercontrol.rest.annotation.validation.RestValidateString.CheckType;
import com.clustercontrol.util.MessageConstant;

public abstract class AbstractAddFilterSettingRequest extends AbstractFilterSettingRequest {

	@RestItemName(MessageConstant.FILTER_ID)
	@RestValidateString(notNull = true, type = CheckType.ID, minLen = 1, maxLen = FilterSettingConstant.ID_LEN_MAX)
	private String filterId;

	public String getFilterId() {
		return filterId;
	}

	public void setFilterId(String filterId) {
		this.filterId = filterId;
	}
	
	@Override
	public void correlationCheck() throws InvalidSetting {
		// NOP
	}
}
