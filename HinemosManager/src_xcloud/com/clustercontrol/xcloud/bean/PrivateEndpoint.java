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

import com.clustercontrol.xcloud.model.PrivateEndpointEntity;
import com.clustercontrol.xcloud.validation.annotation.ElementId;
import com.clustercontrol.xcloud.validation.annotation.NotNull;
import com.clustercontrol.xcloud.validation.annotation.Size;

@XmlRootElement(namespace ="http://xcloud.ws.clustercontrol.com")
public class PrivateEndpoint {
	private String endpointId;
	private String url;
	
	public PrivateEndpoint() {
	}

	public PrivateEndpoint(String endpointId, String url) {
		this.endpointId = endpointId;
		this.url = url;
	}

	@ElementId("endpointId")
	@Size(max=256)
	@NotNull
	public String getEndpointId() {
		return endpointId;
	}
	public void setEndpointId(String endpointId) {
		this.endpointId = endpointId;
	}

	@ElementId("url")
	@Size(max=256)
	@NotNull
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	
	public static PrivateEndpoint convertWebEntity(PrivateEndpointEntity entity) {
		PrivateEndpoint webEntity = new PrivateEndpoint();
		
		webEntity.setEndpointId(entity.getEndpointId());
		webEntity.setUrl(entity.getUrl());
		
		return webEntity;
	}

	public static List<PrivateEndpoint> convertWebEntities(List<PrivateEndpointEntity> entities) {
		List<PrivateEndpoint> locations = new ArrayList<>();
		for (PrivateEndpointEntity endpoint: entities) {
			locations.add(convertWebEntity(endpoint));
		}
		return locations;
	}
}
