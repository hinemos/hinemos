/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.clustercontrol.xcloud.model.EndpointEntity;

@XmlRootElement(namespace ="http://xcloud.ws.clustercontrol.com")
public class Endpoint {
	private String id;
	private String url;
	
	public Endpoint() {
	}

	public Endpoint(String id, String url) {
		this.id = id;
		this.url = url;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public static Endpoint convertWebEntity(EndpointEntity entity) {
		Endpoint webEntity = new Endpoint();
		
		webEntity.setId(entity.getEndpointId());
		webEntity.setUrl(entity.getUrl());
		
		return webEntity;
	}

	public static List<Endpoint> convertWebEntities(List<EndpointEntity> entities) {
		List<Endpoint> locations = new ArrayList<>();
		for (EndpointEntity endpoint: entities) {
			locations.add(convertWebEntity(endpoint));
		}
		return locations;
	}
}
