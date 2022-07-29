/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorresult.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;

public class ModifyCollectGraphFlgRequest implements RequestDto{

	public ModifyCollectGraphFlgRequest(){
	}

	private List<EventLogInfoRequest> list; 
	private Boolean collectGraphFlg;
	
	public List<EventLogInfoRequest> getList(){
		return this.list;
	}
	
	public void setList(List<EventLogInfoRequest> list){
		this.list = list;
	}
	
	public Boolean getCollectGraphFlg(){
		return this.collectGraphFlg;
	}
	
	public void setCollectGraphFlg(Boolean collectGraphFlg){
		this.collectGraphFlg = collectGraphFlg;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
