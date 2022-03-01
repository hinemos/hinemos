/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorresult.dto;

import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class ExecEventCustomCommandRequest implements RequestDto{

	public ExecEventCustomCommandRequest(){
	}

	private int commandNo;
	private List<EventLogInfoRequest> eventList;

	public int getCommandNo(){
		return this.commandNo;
	}

	public void setCommandNo(int commandNo){
		this.commandNo = commandNo;
	}

	public List<EventLogInfoRequest> getEventList(){
		return this.eventList;
	}

	public void setEventList(List<EventLogInfoRequest> eventList){
		this.eventList = eventList;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
