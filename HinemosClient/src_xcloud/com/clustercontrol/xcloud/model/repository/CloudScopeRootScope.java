/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.repository;

import org.openapitools.client.model.HFacilityResponse;

public class CloudScopeRootScope extends Scope implements ICloudScopeRootScope {
	private CloudRepository cloudRepository;
	
	public CloudScopeRootScope(CloudRepository cloudRepository) {
		this.cloudRepository = cloudRepository;
	}
	
	@Override
	public CloudRepository getCloudRepository() {
		return cloudRepository;
	}

	@Override
	public CloudScopeScope getCloudScopeScope() {
		return null;
	}

	@Override
	public LocationScope getLocationScope() {
		return null;
	}

	public static CloudScopeRootScope convert(CloudRepository cloudRepository, HFacilityResponse source) {
		CloudScopeRootScope scope = new CloudScopeRootScope(cloudRepository);
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
