/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.sdml.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class GetSdmlControlSettingListRequest implements RequestDto {

	private SdmlControlSettingFilterInfoRequest sdmlControlSettingFilterInfo;

	public GetSdmlControlSettingListRequest() {
	}

	public SdmlControlSettingFilterInfoRequest getSdmlControlSettingFilterInfo() {
		return sdmlControlSettingFilterInfo;
	}

	public void setSdmlControlSettingFilterInfo(SdmlControlSettingFilterInfoRequest sdmlControlSettingFilterInfo) {
		this.sdmlControlSettingFilterInfo = sdmlControlSettingFilterInfo;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
