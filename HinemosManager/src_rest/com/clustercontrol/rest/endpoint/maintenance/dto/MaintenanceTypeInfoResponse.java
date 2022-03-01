/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.maintenance.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.endpoint.maintenance.dto.enumtype.MaintenanceType;

public class MaintenanceTypeInfoResponse {
	@RestBeanConvertEnum
	private MaintenanceType typeId;
	private String nameId;
	private Integer orderNo;
	
	public MaintenanceTypeInfoResponse() {
	}

	@Override
	public String toString() {
		return "MaintenanceInfoResponse [typeId=" + typeId
				+ ", nameId=" + nameId + ", orderNo=" + orderNo + "]";
	}

	public MaintenanceType getTypeId() {
		return typeId;
	}

	public void setTypeId(MaintenanceType typeId) {
		this.typeId = typeId;
	}

	public String getNameId() {
		return nameId;
	}

	public void setNameId(String nameId) {
		this.nameId = nameId;
	}

	public Integer getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(Integer orderNo) {
		this.orderNo = orderNo;
	}
}
