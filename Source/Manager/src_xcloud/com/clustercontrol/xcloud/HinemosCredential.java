/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud;

public class HinemosCredential {
	public static final String ANONYMOUS_USER = "anonymous";
	
	private String userId;

	public HinemosCredential(String userId) {
		this.userId = userId;
	}
	
	public HinemosCredential() {
		this.userId = ANONYMOUS_USER;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}
