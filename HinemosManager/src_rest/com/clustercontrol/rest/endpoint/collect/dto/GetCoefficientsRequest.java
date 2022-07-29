/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.collect.dto;

public class GetCoefficientsRequest {

	public GetCoefficientsRequest(){
	}

	private String monitorId;

	private String facilityId;

	private String displayName;

	private String itemName;
	
	public String getMonitorId(){
		return this.monitorId;
	}
	
	public void setMonitorId(String monitorId){
		this.monitorId = monitorId;
	}
	
	public String getFacilityId(){
		return this.facilityId;
	}
	
	public void setFacilityId(String facilityId){
		this.facilityId = facilityId;
	}
	
	public String getDisplayName(){
		return this.displayName;
	}
	
	public void setDisplayName(String displayName){
		this.displayName = displayName;
	}
	
	public String getItemName(){
		return this.itemName;
	}
	
	public void setItemName(String item_name){
		this.itemName = item_name;
	}
}
