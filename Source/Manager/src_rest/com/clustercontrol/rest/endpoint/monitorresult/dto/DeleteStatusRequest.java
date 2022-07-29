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

public class DeleteStatusRequest implements RequestDto{

	public DeleteStatusRequest(){
	}
	
	private List<StatusDataInfoRequestP1> statusDataInfoRequestlist;
	
	public List<StatusDataInfoRequestP1> getStatusDataInfoRequestlist(){
		return this.statusDataInfoRequestlist;
	}
	
	public void setStatusDataInfoRequestlist(List<StatusDataInfoRequestP1> statusDataInfoRequestlist){
		this.statusDataInfoRequestlist= statusDataInfoRequestlist;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
