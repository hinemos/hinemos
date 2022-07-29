/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.collect.dto;

import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertDatetime;

public class LogCountRunResultResponse {

	public LogCountRunResultResponse(){
	}
	
	private String monitorId;

	@RestBeanConvertDatetime
	private String startDate;

	public String getMonitorId(){
		return this.monitorId;
	}

	public void setMonitorId(String monitorId){
		this.monitorId = monitorId;
	}

	public String getStartDate(){
		return this.startDate;
	}

	public void setStartDate(String startDate){
		this.startDate = startDate;
	}
}
