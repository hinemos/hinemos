/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;

import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.common.ErrorCode;

@Entity
@DiscriminatorValue(PrivateCloudScopeEntity.typeName)
public class PrivateCloudScopeEntity extends CloudScopeEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String typeName = "Private";
	
	private Map<String, PrivateLocationEntity> privateLocations = new HashMap<String, PrivateLocationEntity>();
	
	public PrivateCloudScopeEntity() {
	}

	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	@JoinColumn(name="cloud_scope_id", referencedColumnName="cloud_scope_id", nullable=false)
	@MapKey(name="locationId")
	public Map<String, PrivateLocationEntity> getPrivateLocations() {
		return this.privateLocations;
	}
	public void setPrivateLocations(Map<String, PrivateLocationEntity> privateLocations) {
		this.privateLocations = privateLocations;
	}
	
	@Override
	public List<LocationEntity> getLocations() throws CloudManagerException {
		List<LocationEntity> locations = new ArrayList<>();
		for (PrivateLocationEntity privateLocation: getPrivateLocations().values()) {
			locations.add(privateLocation.toLocationEntity(this));
		}
		return locations;
	}
	
	@Override
	public LocationEntity getLocation(String locationId) throws CloudManagerException {
		PrivateLocationEntity privateLocation = getPrivateLocations().get(locationId);
		if (privateLocation == null)
			throw ErrorCode.LOCATION_NOT_FOUND.cloudManagerFault(getCloudScopeId(), locationId);
		return privateLocation.toLocationEntity(this);
	}
	
	@Override
	public void visit(IVisitor visitor) throws CloudManagerException {
		visitor.visit(this);
	}
	@Override
	public <T> T transform(ITransformer<T> transformer) throws CloudManagerException {
		return transformer.transform(this);
	}
	
	@Override
	public boolean isPublic() {
		return false;
	}

	private void writeObject(ObjectOutputStream stream) throws IOException {
	}
	
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
	}
}
