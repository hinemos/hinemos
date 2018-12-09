/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.repository;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.xcloud.model.cloud.HinemosManager;

public class Scope extends Facility implements IScope {
	protected Scope parent;
	protected List<Facility> facilities = new ArrayList<Facility>();

	public Scope() {
	}
	
	@Override
	public Scope getParent() {
		return parent;
	}
	
	public void setParent(Scope parent) {
		this.parent = parent;
	}

	@Override
	public Facility[] getFacilities() {
		return facilities.toArray(new Facility[facilities.size()]);
	}

	public void addFacility(Facility child) {
		if (!facilities.contains(child)) {
			if (child instanceof Scope) {
				((Scope)child).setParent(this);
			} else if (child instanceof Node) {
				((Node)child).setParent(this);
			}
			internalAddProperty(IScope.p.facilities, child, facilities);
		}
	}
	
	public void removeFacility(Facility child) {
		if (facilities.contains(child)) {
			if (child instanceof Scope) {
				((Scope)child).setParent(null);
			} else if (child instanceof Node) {
				((Node)child).setParent(null);
			}
			internalRemoveProperty(IScope.p.facilities, child, facilities);
		}
	}

	@Override
	public HinemosManager getHinemosManager() {
		return getCloudRepository().getHinemosManager();
	}
	
	public static Scope convert(com.clustercontrol.ws.xcloud.HScope source) {
		Scope scope = new Scope();
		scope.update(source);
		return scope;
	}
	
	@Override
	public CloudRepository getCloudRepository() {
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
	public void visit(IVisitor visitor) {
		visitor.visit(this);
	}
	@Override
	public <T> T transform(ITransformer<T> transformor) {
		return transformor.transform(this);
	}
}
