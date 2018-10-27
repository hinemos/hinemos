/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import javax.xml.bind.annotation.XmlIDREF;

import com.clustercontrol.xcloud.CloudManagerException;

public class HInstanceNode extends HNode {
	protected Instance instance;
	
	public HInstanceNode() {
		super(FacilityType.Instance);
	}
	
	@XmlIDREF
	public Instance getInstance() {
		return instance;
	}
	public void setInstance(Instance instance) {
		this.instance = instance;
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
