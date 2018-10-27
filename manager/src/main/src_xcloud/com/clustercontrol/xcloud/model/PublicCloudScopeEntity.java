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
import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.InternalManagerError;
import com.clustercontrol.xcloud.Session.SessionScope;
import com.clustercontrol.xcloud.factory.IPrivateCloudOption;
import com.clustercontrol.xcloud.factory.IPublicCloudOption;

@Entity
@DiscriminatorValue(PublicCloudScopeEntity.typeName)
public class PublicCloudScopeEntity extends CloudScopeEntity implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String typeName = "Public";

	protected List<LocationEntity> locations;

	@Override
	public List<LocationEntity> getLocations() throws CloudManagerException {
		if (locations != null) {
			return locations;
		} else {
			try (SessionScope sessionScope = SessionScope.open()) {
				locations = callOptionEx(new OptionCallableEx<List<LocationEntity>>() {
					@Override
					public List<LocationEntity> call(PublicCloudScopeEntity scope, IPublicCloudOption option) throws CloudManagerException {
						List<LocationEntity> locations = new ArrayList<>();
						for (PublicLocationEntity location: option.getLocations(scope)) {
							locations.add(location.toLocationEntity(PublicCloudScopeEntity.this));
						}
						return locations;
					}
					@Override
					public List<LocationEntity> call(PrivateCloudScopeEntity scope, IPrivateCloudOption option) throws CloudManagerException {
						throw new InternalManagerError();
					}
				});
				return locations;
			}
		}
	}

	@Override
	public LocationEntity getLocation(final String locationId) throws CloudManagerException {
		for (LocationEntity location: getLocations()) {
			if (location.getLocationId().equals(locationId))
				return location;
		}
		return null;
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
		return true;
	}

	private void writeObject(ObjectOutputStream stream) throws IOException {
	}
	
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
	}
}
