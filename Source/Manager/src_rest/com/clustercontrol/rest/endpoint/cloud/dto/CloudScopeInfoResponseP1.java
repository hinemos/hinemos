/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.cloud.dto;

public class CloudScopeInfoResponseP1 {

	private String name;
	private String description;
	private CredentialResponse credential;

	public CloudScopeInfoResponseP1() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public CredentialResponse getCredential() {
		return credential;
	}

	public void setCredential(CredentialResponse credential) {
		this.credential = credential;
	}

}
