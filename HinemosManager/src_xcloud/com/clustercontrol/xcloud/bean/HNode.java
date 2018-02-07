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

import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlList;

import com.clustercontrol.xcloud.CloudManagerException;

public class HNode extends HFacility {
	private List<HScope> parents = new ArrayList<>();
	
	public HNode() {
		super(FacilityType.Node);
	}
	
	public HNode(FacilityType type) {
		super(type);
	}

	@XmlIDREF 
	@XmlList
	public List<HScope> getParents() {
		return parents;
	}
	public void setParents(List<HScope> parents) {
		this.parents = parents;
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
