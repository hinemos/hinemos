/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;


public class PublicEndpointEntity {
	private PublicLocationEntity location;
	private String id;
	private String url;

	public PublicEndpointEntity()	{
	}

	public PublicEndpointEntity(PublicLocationEntity location, String id, String url) {
		setLocation(location);
		setId(id);
		setURL(url);
	}

	public PublicLocationEntity getLocation() {
		return location;
	}
	public void setLocation(PublicLocationEntity location) {
		this.location = location;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getURL() {
		return this.url;
	}
	public void setURL( String url ) {
		this.url = url;
	}

	public EndpointEntity toEndpointEntity(LocationEntity location) {
		EndpointEntity endpoint = new EndpointEntity();
		endpoint.setLocation(location);
		endpoint.setEndpoint(this.getId());
		endpoint.setUrl(this.getURL());
		return endpoint;
	}
}
