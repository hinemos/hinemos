/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;


public class EndpointEntity {
	private LocationEntity location;
	private String endpoinId;
	private String url;

	public EndpointEntity()	{
	}

	public EndpointEntity(String id, String url) {
		setEndpoint(id);
		setUrl(url);
	}

	public EndpointEntity(EndpointEntity otherData) {
		setEndpoint(otherData.getEndpointId());
		setUrl(otherData.getUrl());
	}

	public LocationEntity getLocation() {
		return location;
	}
	public void setLocation(LocationEntity location) {
		this.location = location;
	}

	public String getEndpointId() {
		return endpoinId;
	}
	public void setEndpoint(String id) {
		this.endpoinId = id;
	}
	
	public String getUrl() {
		return this.url;
	}
	public void setUrl( String url ) {
		this.url = url;
	}
}
