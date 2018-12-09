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

import com.clustercontrol.xcloud.CloudManagerException;

public class HLocationScope extends HScope {
	private Location location;
	private List<HInstanceNode> instanceNodes = new ArrayList<>();
	
	public HLocationScope() {
	}
	
	public Location getLocation() {
		return location;
	}
	public void setLocation(Location location) {
		this.location = location;
	}

	public List<HInstanceNode> getInstanceNodes() {
		return instanceNodes;
	}
	public void setInstanceNodes(List<HInstanceNode> instanceNodes) {
		this.instanceNodes = instanceNodes;
	}
	
	@Override
	public void visit(IVisitor visitor) throws CloudManagerException {
		visitor.visit(this);
	}
	
	@Override
	public <T> T transform(ITransformer<T> tranformer) throws CloudManagerException {
		return tranformer.transform(this);
	}
}
