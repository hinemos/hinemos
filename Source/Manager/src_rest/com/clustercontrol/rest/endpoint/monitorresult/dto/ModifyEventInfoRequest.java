/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorresult.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class ModifyEventInfoRequest implements RequestDto{

	public ModifyEventInfoRequest(){
	}
	private EventLogInfoRequest info;
	
	public EventLogInfoRequest getInfo(){
		return this.info;
	}
	
	public void setInfo(EventLogInfoRequest info){
		this.info = info;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
