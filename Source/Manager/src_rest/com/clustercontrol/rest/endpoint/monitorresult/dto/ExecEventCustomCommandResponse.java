/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorresult.dto;

public class ExecEventCustomCommandResponse {

	public ExecEventCustomCommandResponse(){
	}
	
	private String commandResultID;
	
	public String getCommandResultID(){
		return this.commandResultID;
	}
	
	public void setCommandResultID(String commandResultID){
		this.commandResultID = commandResultID;
	}
}
