/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;


public class CloudSpec {
	private boolean publicCloud;
	private boolean cloudServiceMonitorEnabled;
	private boolean billingAlarmEnabled;
	
	public CloudSpec() {
	}
	
	public CloudSpec(boolean publicCloud, boolean cloudServiceMonitorEnabled, boolean billingAlarmEnabled) {
		super();
		this.publicCloud = publicCloud;
		this.cloudServiceMonitorEnabled = cloudServiceMonitorEnabled;
		this.billingAlarmEnabled = billingAlarmEnabled;
	}

	public boolean isPublicCloud() {
		return publicCloud;
	}
	public void setPublicCloud(boolean publicCloud) {
		this.publicCloud = publicCloud;
	}
	public boolean isCloudServiceMonitorEnabled() {
		return cloudServiceMonitorEnabled;
	}
	public void setCloudServiceMonitorEnabled(boolean cloudServiceMonitorEnabled) {
		this.cloudServiceMonitorEnabled = cloudServiceMonitorEnabled;
	}
	public boolean isBillingAlarmEnabled() {
		return billingAlarmEnabled;
	}
	public void setBillingAlarmEnabled(boolean billingAlarmEnabled) {
		this.billingAlarmEnabled = billingAlarmEnabled;
	}
}
