/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorresult.dto;

public class GetEventCustomCommandResultResponse {

	public GetEventCustomCommandResultResponse(){
	}

	private EventCustomCommandResultRootResponse eventCustomCommandResultRoot;

	public EventCustomCommandResultRootResponse getEventCustomCommandResultRoot(){
		return this.eventCustomCommandResultRoot;
	}

	public void setEventCustomCommandResultRoot(EventCustomCommandResultRootResponse eventCustomCommandResultRoot){
		this.eventCustomCommandResultRoot = eventCustomCommandResultRoot;
	}
}
