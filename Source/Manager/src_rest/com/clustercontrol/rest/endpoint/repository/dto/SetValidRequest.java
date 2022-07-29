/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.repository.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class SetValidRequest implements RequestDto {

	private Boolean flg;

	public SetValidRequest() {
	}

	public Boolean getFlg() {
		return flg;
	}

	public void setFlg(Boolean flg) {
		this.flg = flg;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
