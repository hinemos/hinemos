/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.utility.dto;

public class ImportMonitorCommonRecordRequest extends AbstractImportRecordRequest<MonitorInfoRequestForUtility> {
	String monitorModule ;

	public ImportMonitorCommonRecordRequest(){
	}

	public String getMonitorModule() {
		return monitorModule;
	}
	public void setMonitorModule(String monitorModule) {
		this.monitorModule = monitorModule;
	}
	
	
}
