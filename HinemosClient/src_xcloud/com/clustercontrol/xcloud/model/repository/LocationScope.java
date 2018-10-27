/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.repository;

import com.clustercontrol.xcloud.model.cloud.Location;


public class LocationScope extends Scope implements ILocationScope {
	protected Location location;
	@Override
	public Location getLocation() {
		return location;
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}

	@Override
	public CloudScopeScope getCloudScopeScope() {
		return parent.getCloudScopeScope();
	}

	@Override
	public LocationScope getLocationScope() {
		return this;
	}
	
	public static LocationScope convert(com.clustercontrol.ws.xcloud.HLocationScope source) {
		LocationScope scope = new LocationScope();
		scope.update(source);
		return scope;
	}

	@Override
	public void visit(IVisitor visitor) {
		visitor.visit(this);
	}
	@Override
	public <T> T transform(ITransformer<T> transformor) {
		return transformor.transform(this);
	}
}
