/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.bean;

public enum RestKind {
	
	AccessRestEndpoints("AccessRestEndpoints"),
	CalendarRestEndpoints("CalendarRestEndpoints"),
	HubRestEndpoints("HubRestEndpoints"),
	JobRestEndpoints("JobRestEndpoints"),
	CommonRestEndpoints("CommonRestEndpoints"),
	MaintenanceRestEndpoints("MaintenanceRestEndpoints"),
	JobMapRestEndpoints("JobMapRestEndpoints"),
	MonitorsettingRestEndpoints("MonitorsettingRestEndpoints"),
	NotifyRestEndpoints("NotifyRestEndpoints"),
	RepositoryRestEndpoints("RepositoryRestEndpoints"),
	CollectRestEndpoints("CollectRestEndpoints"),
	ReportingRestEndpoints("ReportingRestEndpoints"),
	InfraRestEndpoints("InfraRestEndpoints"),
	NodeMapRestEndpoints("NodeMapRestEndpoints"),
	MonitorResultRestEndpoints("MonitorResultRestEndpoints"),
	UtilityRestEndpoints("UtilityRestEndpoints"),
	FilterSettingRestEndpoints("FilterSettingRestEndpoints"),
	SdmlRestEndpoints("SdmlRestEndpoints"),
	SdmlUtilityRestEndpoints("SdmlUtilityRestEndpoints"),
	CloudRestEndpoints("CloudRestEndpoints"),
	VMwareOptionRestEndpoints("VMwareOptionRestEndpoints"),
	AWSOptionRestEndpoints("AWSOptionRestEndpoints"),
	RpaRestEndpoints("RpaRestEndpoints");

	private final String name;

	private RestKind(final String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	
}
