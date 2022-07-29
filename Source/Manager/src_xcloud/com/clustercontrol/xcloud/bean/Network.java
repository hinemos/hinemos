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

public class Network extends LocationResource {
	private List<String> attachedInstances = new ArrayList<>();
	
	public Network() {
	}
	
	public List<String> getAttachedInstances() {
		return attachedInstances;
	}
	public void setAttachedInstances(List<String> attachedInstances) {
		this.attachedInstances = attachedInstances;
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
