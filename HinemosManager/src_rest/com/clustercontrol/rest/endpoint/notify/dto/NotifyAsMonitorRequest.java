/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.notify.dto;

import java.util.ArrayList;

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.annotation.RestItemName;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.notify.dto.enumtype.NotifyPriorityEnum;
import com.clustercontrol.util.MessageConstant;

public class NotifyAsMonitorRequest implements RequestDto {

	@RestItemName(value = MessageConstant.PLUGIN_ID)
	private String pluginId;

	@RestItemName(value = MessageConstant.MONITOR_ID)
	private String monitorId;

	@RestItemName(value = MessageConstant.FACILITY_ID)
	private String facilityId;

	private String subKey;

	@RestBeanConvertDatetime
	private String generationDate;

	@RestItemName(value = MessageConstant.PRIORITY)
	@RestBeanConvertEnum
	private NotifyPriorityEnum priority;

	@RestItemName(value = MessageConstant.APPLICATION)
	private String application;

	@RestItemName(value = MessageConstant.MESSAGE)
	private String message;

	@RestItemName(value = MessageConstant.MESSAGE_ORG)
	private String messageOrg;

	private ArrayList<String> notifyIdList;

	private String srcId;

	public NotifyAsMonitorRequest() {
	}

	public String getPluginId() {
		return pluginId;
	}

	public void setPluginId(String pluginId) {
		this.pluginId = pluginId;
	}

	public String getMonitorId() {
		return monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getSubKey() {
		return subKey;
	}

	public void setSubKey(String subKey) {
		this.subKey = subKey;
	}

	public String getGenerationDate() {
		return generationDate;
	}

	public void setGenerationDate(String generationDate) {
		this.generationDate = generationDate;
	}

	public NotifyPriorityEnum getPriority() {
		return priority;
	}

	public void setPriority(NotifyPriorityEnum priority) {
		this.priority = priority;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessageOrg() {
		return messageOrg;
	}

	public void setMessageOrg(String messageOrg) {
		this.messageOrg = messageOrg;
	}

	public ArrayList<String> getNotifyIdList() {
		return notifyIdList;
	}

	public void setNotifyIdList(ArrayList<String> notifyIdList) {
		this.notifyIdList = notifyIdList;
	}

	public String getSrcId() {
		return srcId;
	}

	public void setSrcId(String srcId) {
		this.srcId = srcId;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}
}
