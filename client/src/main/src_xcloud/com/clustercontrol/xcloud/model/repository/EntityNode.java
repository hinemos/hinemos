/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.repository;


public class EntityNode extends Node implements IEntityNode {
	private String entityType;

	public static EntityNode convert(com.clustercontrol.ws.xcloud.HEntityNode source) {
		EntityNode node = new EntityNode();
		node.update(source);
		node.setEntityType(source.getEntityType());
		return node;
	}
	
	@Override
	public void visit(IVisitor visitor) {
		visitor.visit(this);
	}
	@Override
	public <T> T transform(ITransformer<T> transformor) {
		return transformor.transform(this);
	}

	@Override
	public String getEntityType() {
		return entityType;
	}
	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}
}
