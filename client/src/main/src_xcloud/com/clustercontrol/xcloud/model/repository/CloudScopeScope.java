/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.repository;

import com.clustercontrol.xcloud.model.cloud.CloudScope;
import com.clustercontrol.xcloud.model.cloud.Location;


public class CloudScopeScope extends Scope implements ICloudScopeScope {
	private CloudScope cloudScope;
	private Location location;
	
	@Override
	public CloudScope getCloudScope() {
		return cloudScope;
	}
	public void setCloudScope(CloudScope cloudScope) {
		this.cloudScope = cloudScope;
	}
	
	@Override
	public Location getLocation() {
		return location;
	}
	public void setLocation(Location location) {
		this.location = location;
	}
	
	@Override
	public CloudScopeScope getCloudScopeScope() {
		return this;
	}

	public static CloudScopeScope convert(com.clustercontrol.ws.xcloud.HCloudScopeScope source) {
		CloudScopeScope scope = new CloudScopeScope();
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
