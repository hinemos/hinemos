/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import com.clustercontrol.xcloud.CloudManagerException;

public class HEntityNode extends HNode {
	private String entityType;

	public HEntityNode() {
		super(FacilityType.Entity);
	}
	
	@Override
	public void visit(IVisitor visitor) throws CloudManagerException {
		visitor.visit(this);
	}

	@Override
	public <T> T transform(ITransformer<T> tranformer) throws CloudManagerException {
		return tranformer.transform(this);
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}
	public String getEntityType() {
		return entityType;
	}
}
