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
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class SetTransferValidRequest implements RequestDto {

	@RestItemName(value = MessageConstant.HUB_TRANSFER_ID)
	private List<String> transferIdList = new ArrayList<>();

	@RestItemName(value = MessageConstant.SETTING_VALID)
	@RestValidateObject(notNull = true)
	private Boolean flg;

	public List<String> getTransferIdList() {
		return transferIdList;
	}
	public void setTransferIdList(List<String> transferIdList) {
		this.transferIdList = transferIdList;
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
