/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud;

public class HinemosCredential {
	private String userId;

	public HinemosCredential(String userId) {
		this.userId = userId;
	}
	
	public HinemosCredential() {
		this.userId = "anonymous";
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}
