/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.repository;

import com.clustercontrol.xcloud.model.cloud.Instance;


public class InstanceNode extends Node implements IInstanceNode{
	private Instance instance;
	
	@Override
	public Instance getInstance() {
		return instance;
	}
	public void setInstance(Instance instance) {
		this.instance = instance;
	}

	public static InstanceNode convert(com.clustercontrol.ws.xcloud.HInstanceNode source) {
		InstanceNode node = new InstanceNode();
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
		return "InstanceNode [instance=" + instance + ", toString()="
				+ super.toString() + "]";
	}
}
