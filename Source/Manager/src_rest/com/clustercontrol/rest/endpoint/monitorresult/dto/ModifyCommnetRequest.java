/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorresult.dto;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.dto.RequestDto;

public class ModifyCommnetRequest implements RequestDto{
	
	public ModifyCommnetRequest(){
	}
	
	private String monitorId;
	private String monitorDetailId;
	private String pluginId;
	private String facilityId;
	@RestBeanConvertDatetime
	private String outputDate; 
	private String comment;
	@RestBeanConvertDatetime
	private String commentDate;
	private String commentUser;

	public String getMonitorId(){
		return this.monitorId ;
	}
	
	public void setMonitorId(String monitorId){
		this.monitorId = monitorId ;
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
	
	public void setpluginId(String pluginId){
		this.pluginId = pluginId;
	}
	
	public String getFacilityId(){
		return this.facilityId;
	}
	
	public void setFacilityId(String facilityId){
		this.facilityId = facilityId;
	}
	
	public String getOutputDate(){
		return this.outputDate ;
	}
	
	public void setOutputDate(String outputDate){
		this.outputDate = outputDate;
	}
	
	public String getComment(){
		return this.comment ;
	}
	
	public void setComment(String comment){
		this.comment = comment;
	}
	
	public String getCommentDate(){
		return this.commentDate ;
	}
	
	public void setCommentDate(String commentDate){
		this.commentDate = commentDate;
	}
	
	public String getCommentUser(){
		return this.commentUser;
	}
	
	public void setCommentUser(String commentUser){
		this.commentUser = commentUser;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
