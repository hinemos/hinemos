/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.clustercontrol.xcloud.model.PrivateLocationEntity;
import com.clustercontrol.xcloud.validation.annotation.ElementId;
import com.clustercontrol.xcloud.validation.annotation.Identity;
import com.clustercontrol.xcloud.validation.annotation.Into;
import com.clustercontrol.xcloud.validation.annotation.NotNull;
import com.clustercontrol.xcloud.validation.annotation.Size;

@XmlRootElement(namespace ="http://xcloud.ws.clustercontrol.com")
public class PrivateLocation {
	private String locationId;
	private String name;
	private List<PrivateEndpoint> endpoints = new ArrayList<>();
	
	public PrivateLocation() {
	}
	
	public PrivateLocation(String locationId, String name, List<PrivateEndpoint> endpoints) {
		this.locationId = locationId;
		this.name = name;
		this.endpoints = endpoints;
	}

	@ElementId("locationId")
	@Identity
	@NotNull
	public String getLocationId() {
		return locationId;
	}
	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}

	@ElementId("locationName")
	@Size(max=256)
	@NotNull
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@ElementId("endpoints")
	@NotNull
	@Into
	public List<PrivateEndpoint> getEndpoints() {
		return endpoints;
	}
	public void setEndpoints(List<PrivateEndpoint> endpoints) {
		this.endpoints = endpoints;
	}
	
	public static PrivateLocation convertWebEntity(PrivateLocationEntity entity) {
		PrivateLocation webEntity = new PrivateLocation();
		webEntity.setLocationId(entity.getLocationId());
		webEntity.setName(entity.getName());
		webEntity.getEndpoints().addAll(PrivateEndpoint.convertWebEntities(entity.getEndpoints()));
		return webEntity;
	}

	public static List<PrivateLocation> convertWebEntities(Collection<PrivateLocationEntity> entities) {
		List<PrivateLocation> locations = new ArrayList<>();
		Iterator<PrivateLocationEntity> iter = entities.iterator();
		while (iter.hasNext()) {
			locations.add(convertWebEntity(iter.next()));
		}
		return locations;
	}
}
