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

public class HScope extends HFacility {
	private List<HFacility> facilities = new ArrayList<>();
	private HScope parent;

	public HScope() {
		super(FacilityType.Scope);
	}

	public HScope(FacilityType type) {
		super(type);
	}
	
	@XmlIDREF
	public HScope getParent() {
		return parent;
	}
	public void setParent(HScope parent) {
		this.parent = parent;
	}

	@XmlIDREF
	@XmlList
	public List<HFacility> getFacilities() {
		return facilities;
	}
	public void setFacilities(List<HFacility> facilities) {
		this.facilities = facilities;
	}
	
	public void addScope(HScope scope) {
		scope.setParent(this);
		this.facilities.add(scope);
	}
	public void removeScope(HScope scope) {
		scope.setParent(null);
		this.facilities.remove(scope);
	}
	
	public void addNode(HNode node) {
		node.getParents().add(this);
		this.facilities.add(node);
	}
	public void removeNode(HNode node) {
		node.getParents().remove(this);
		this.facilities.remove(node);
	}
	
	public void addFacilities(List<HFacility> facilities) {
		for (HFacility facility: facilities) {
			if (facility instanceof HScope) {
				addScope((HScope)facility);
			} else {
				addNode((HNode)facility);
			}
		}
	}
	public void removeFacilities(List<HFacility> facilities) {
		for (HFacility facility: facilities) {
			if (facility instanceof HScope) {
				removeScope((HScope)facility);
			} else {
				removeNode((HNode)facility);
			}
		}
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
