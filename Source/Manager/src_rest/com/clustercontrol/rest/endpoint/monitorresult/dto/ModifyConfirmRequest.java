/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorresult.dto;

import java.util.ArrayList;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.monitorresult.dto.enumtype.ConfiremTypeEnum;

public class ModifyConfirmRequest implements RequestDto{

	public ModifyConfirmRequest(){
	}
	
	private ArrayList<EventLogInfoRequest> list;
	
	private ConfiremTypeEnum confirmType;
	
	public ArrayList<EventLogInfoRequest> getList(){
		return this.list;
	}
	
	public void setList(ArrayList<EventLogInfoRequest> list){
		this.list = list;
	}
	
	public Integer getConfirmType(){
		return this.confirmType.getCode();
	}
	
	public void setConfirmType(ConfiremTypeEnum confirmType){
		this.confirmType = confirmType;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
