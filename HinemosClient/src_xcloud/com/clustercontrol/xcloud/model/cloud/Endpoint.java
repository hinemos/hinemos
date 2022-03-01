/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import org.openapitools.client.model.EndpointEntityResponse;

import com.clustercontrol.xcloud.model.base.Element;

public class Endpoint extends Element implements IEndpoint {
	private Location location = null;
	private String id = null;
	private String url = null;
	
	public Endpoint() {
	}
	
	@Override
	public Location getLocation() {
		return location;
	}
	public void setLocation(Location location) {
		internalSetProperty(p.location, location, ()->this.location, (s)->this.location=s);
	}

	@Override
	public String getId() {
		return id;
	}
	public void setId(String id) {
		internalSetProperty(p.id, id, ()->this.id, (s)->this.id=s);
	}

	@Override
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		internalSetProperty(p.url, url, ()->this.url, (s)->this.url=s);
	}
	
	public void update(Location location, EndpointEntityResponse endpoint) {
		setLocation(location);
		setId(endpoint.getEndpointId());
		setUrl(endpoint.getUrl());
	}
	
	public static Endpoint convert(Location location, EndpointEntityResponse source) {
		Endpoint endpoint = new Endpoint();
		endpoint.update(location, source);
		return endpoint;
	}
}
