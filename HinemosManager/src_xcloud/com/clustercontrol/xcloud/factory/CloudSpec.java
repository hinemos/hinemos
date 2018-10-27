/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

import com.clustercontrol.xcloud.model.CredentialBaseEntity;



public class CloudSpec {
	private boolean instanceMemo;
	private boolean qublic;
	private Class<? extends CredentialBaseEntity> supportedCredential;
	
	private boolean cloudServiceMonitor;
	private boolean billingAlarm;
	
	public CloudSpec(boolean qublic, boolean instanceMemo, Class<? extends CredentialBaseEntity> supportedCredential) {
		this.instanceMemo = instanceMemo;
		this.qublic = qublic;
		this.supportedCredential = supportedCredential;
	}
	public boolean isPublic() {
		return qublic;
	}

	public Class<? extends CredentialBaseEntity> getSupportedCredential() {
		return supportedCredential;
	}
	
	public boolean isInstanceMemoEnabled() {
		return instanceMemo;
	}
	
	public boolean isCloudServiceMonitorEnabled() {
		return cloudServiceMonitor;
	}
	
	public boolean isBillingAlarmEnabled() {
		return billingAlarm;
	}
}
