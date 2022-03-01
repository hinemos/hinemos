/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorresult.dto;

public class StatusDataInfoRequestP1 {

	public StatusDataInfoRequestP1(){
	}
	
	private String monitorId;
	private String monitorDetailId;
	private String pluginId;
	private String facilityId;
	
	public String getMonitorId(){
		return this.monitorId ;
	}
	
	public void setMonitorId(String monitorId){
		this.monitorId = monitorId;
	}
	
	public String getMonitorDetailId(){
		return this.monitorDetailId;
	}
	
	public void setMonitorDetailId(String monitorDetailId){
		this.monitorDetailId = monitorDetailId;
	}

	public String getPluginId(){
		return this.pluginId;
	}
	
	public void get (String pluginId){
		this.pluginId = pluginId;
	}
	//findbugs対応 リフレクションで値が設定される前提のメンバーなので本来不要だが、 セッターを追加
	public void setFacilityId( String  facilityId){
		this.facilityId = facilityId;
	}
	
	public String getFacilityId(){
		return this.facilityId;
	}
	
}
