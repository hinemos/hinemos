/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorsetting.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.validation.RestValidateInteger;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.util.MessageConstant;

public class GetMonitorBeanListRequest implements RequestDto {

	public GetMonitorBeanListRequest() {

	}

	private MonitorFilterInfoRequest monitorFilterInfo = new MonitorFilterInfoRequest();

	// 取得されるレコードの上限数
	@RestItemName(value=MessageConstant.SIZE)
	@RestValidateInteger(notNull = false , minVal = 1)
	private Integer size ;

	public MonitorFilterInfoRequest getMonitorFilterInfo() {
		return monitorFilterInfo;
	}

	public void setMonitorFilterInfo(MonitorFilterInfoRequest monitorFilterInfo) {
		this.monitorFilterInfo = monitorFilterInfo;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	@Override
	public String toString() {
		return "GetMonitorBeanListRequest [monitorFilterInfo=" + monitorFilterInfo + "]";
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
