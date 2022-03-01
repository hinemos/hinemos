/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

public class CloudSpecResponse {
	private Boolean publicCloud;
	private Boolean cloudServiceMonitorEnabled;
	private Boolean billingAlarmEnabled;

	public CloudSpecResponse() {
	}

	public Boolean getPublicCloud() {
		return publicCloud;
	}

	public void setPublicCloud(Boolean publicCloud) {
		this.publicCloud = publicCloud;
	}

	public Boolean getCloudServiceMonitorEnabled() {
		return cloudServiceMonitorEnabled;
	}

	public void setCloudServiceMonitorEnabled(Boolean cloudServiceMonitorEnabled) {
		this.cloudServiceMonitorEnabled = cloudServiceMonitorEnabled;
	}

	public Boolean getBillingAlarmEnabled() {
		return billingAlarmEnabled;
	}

	public void setBillingAlarmEnabled(Boolean billingAlarmEnabled) {
		this.billingAlarmEnabled = billingAlarmEnabled;
	}
}
