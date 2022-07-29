/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.model.PublicCloudScopeEntity;

public class PublicCloudScope extends CloudScope {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private PublicCloudScopeEntity entity;
	
	public PublicCloudScope() {
	}
	public PublicCloudScope(PublicCloudScopeEntity entity) {
		this.entity = entity;
	}

	@Override
	public PublicCloudScopeEntity getEntity() {
		return entity;
	}
	
	@Override
	public void visit(IVisitor visitor) throws CloudManagerException {
		visitor.visit(this);
	}
	@Override
	public <T> T transform(ITransformer<T> transformer) throws CloudManagerException {
		return transformer.transform(this);
	}
}
