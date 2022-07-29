/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.repository;

import org.openapitools.client.model.HFacilityResponse;

import com.clustercontrol.xcloud.model.cloud.HinemosManager;



public class Node extends Facility implements INode {
	private Scope parent;

	@Override
	public Scope getParent() {
		return parent;
	}
	public void setParent(Scope parent) {
		this.parent = parent;
	}

	@Override
	public ICloudRepository getCloudRepository() {
		return parent.getCloudRepository();
	}

	@Override
	public CloudScopeScope getCloudScopeScope() {
		return parent.getCloudScopeScope();
	}

	@Override
	public LocationScope getLocationScope() {
		return parent.getLocationScope();
	}

	@Override
	public HinemosManager getHinemosManager() {
		return parent.getHinemosManager();
	}
	
	public static Node convert(HFacilityResponse source) {
		Node node = new Node();
		node.update(source);
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
	public String toString() {
		return "Node [toString()=" + super.toString() + "]";
	}
}
