/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.maintenance.dto;

import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateObject;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class SetMaintenanceStatusRequest implements RequestDto {
	@RestItemName(value = MessageConstant.MAINTENANCE_ID)
	private List<String> maintenanceIdList;
	
	@RestItemName(value = MessageConstant.VALID_FLG)
	@RestValidateObject(notNull=true)
	private Boolean flg;
	
	public SetMaintenanceStatusRequest() {
	}
	public List<String> getMaintenanceIdList() {
		return maintenanceIdList;
	}
	public void setMaintenanceIdList(List<String> maintenanceIdList) {
		this.maintenanceIdList = maintenanceIdList;
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
