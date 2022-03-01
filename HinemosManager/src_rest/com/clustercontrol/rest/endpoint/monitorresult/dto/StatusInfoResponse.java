/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.monitorresult.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;

public class StatusInfoResponse {

	public StatusInfoResponse(){
	}
	
	private String monitorId = null;
	private String monitorDetailId = null;
	private String pluginId = null;
	private String facilityId = null;
	@RestPartiallyTransrateTarget
	private String application = null;
	@RestBeanConvertDatetime
	private String generationDate = null;
	@RestPartiallyTransrateTarget
	private String message = null;
	@RestBeanConvertDatetime
	private String outputDate = null;
	private Integer priority = null;
	@RestPartiallyTransrateTarget
	private String facilityPath = null;
	private String ownerRoleId = null;

	public String getMonitorId() {
		return this.monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	public String getMonitorDetailId() {
		return monitorDetailId;
	}

	public void setMonitorDetailId(String monitorDetailId) {
		this.monitorDetailId = monitorDetailId;
	}

	public String getPluginId() {
		return this.pluginId;
	}

	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}

	public String getFacilityId() {
		return this.facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getApplication() {
		return this.application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getGenerationDate() {
		return this.generationDate;
	}

	public void setGenerationDate(String generationDate) {
		this.generationDate = generationDate;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getOutputDate() {
		return this.outputDate;
	}

	public void setOutputDate(String outputDate) {
		this.outputDate = outputDate;
	}

	public Integer getPriority() {
		return this.priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	/**
	 * ファシリティパスを返します
	 * 
	 * @return 重要度
	 * @ejb.interface-method
	 * 
	 */
	public String getFacilityPath() {
		return facilityPath;
	}

	/**
	 * ファシリティパスを設定します
	 * 
	 * @param facilityPath
	 * @ejb.interface-method
	 * 
	 */
	public void setFacilityPath(String facilityPath) {
		this.facilityPath = facilityPath;
	}

	/**
	 * オーナーロールIDを返します
	 * 
	 * @return オーナーロールID
	 * 
	 */
	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	/**
	 * オーナーロールIDを設定します
	 * 
	 * @param ownerRoleId
	 * 
	 */
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}
}
