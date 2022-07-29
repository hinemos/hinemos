/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorresult.dto;

import java.util.ArrayList;
import java.util.Map;

public class GetEventDataMapResponse{

	public GetEventDataMapResponse(){
	}
	
	private Map<String, ArrayList<EventLogInfoResponse>> map;
	
	public Map<String, ArrayList<EventLogInfoResponse>> getMap(){
		return this.map;
	}
	
	public void setMap(Map<String, ArrayList<EventLogInfoResponse>> map){
		this.map= map;
	}
}
