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
import com.clustercontrol.xcloud.model.LocationEntity;

@XmlRootElement(namespace ="http://xcloud.ws.clustercontrol.com")
public class Location {
	public static enum EntryType {
		cloud,
		user
	}
	
	private String id;
	private String locationType;
	private String name;
	private EntryType entryType;
	private List<Endpoint> endpoints = new ArrayList<>();
	
	public Location() {
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	public String getLocationType() {
		return locationType;
	}
	public void setLocationType(String locationType) {
		this.locationType = locationType;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public EntryType getEntryType() {
		return entryType;
	}
	public void setEntryType(EntryType entryType) {
		this.entryType = entryType;
	}
	
	public List<Endpoint> getEndpoints() {
		return endpoints;
	}
	public void setEndpoints(List<Endpoint> endpoints) {
		this.endpoints = endpoints;
	}
	
	public static Location convertWebEntity(LocationEntity entity) {
		Location webEntity = new Location();
		
		webEntity.setId(entity.getLocationId());
		webEntity.setName(entity.getName());
		webEntity.setLocationType(entity.getLocationType());
		webEntity.setEntryType(EntryType.valueOf(entity.getEntryType().name()));
		for (EndpointEntity endpointEntity: entity.getEndpoints().values()) {
			webEntity.getEndpoints().add(Endpoint.convertWebEntity(endpointEntity));
		}
		return webEntity;
	}

	public static List<Location> convertWebEntities(List<LocationEntity> entities) {
		List<Location> locations = new ArrayList<>();
		for (LocationEntity endpoint: entities) {
			locations.add(convertWebEntity(endpoint));
		}
		return locations;
	}
}
