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
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public abstract class AbstractFilterSettingRequest implements RequestDto {

	@RestItemName(MessageConstant.FILTER_NAME)
	@RestValidateString(notNull = true, minLen = 1, maxLen = FilterSettingConstant.NAME_LEN_MAX)
	private String filterName;

	@RestItemName(MessageConstant.OWNER_ROLE_ID)
	// validate メソッドで値チェック
	private String ownerRoleId;

	@Override
	public void correlationCheck() throws InvalidSetting {
		// NOP
	}

	public String getFilterName() {
		return filterName;
	}

	public void setFilterName(String filterName) {
		this.filterName = filterName;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}
	
}
