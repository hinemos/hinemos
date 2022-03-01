/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.accesscontrol.bean;

import java.util.ArrayList;
import java.util.List;

public class RoleTreeItemWrapper {
	private RoleTreeItemWrapper parent;
	private Object data;
	private final List<RoleTreeItemWrapper> children = new ArrayList<RoleTreeItemWrapper>();

	public RoleTreeItemWrapper(){
		
	}
	
	public RoleTreeItemWrapper getParent(){
		return this.parent;
	}
	
	public void setParent(RoleTreeItemWrapper parent){
		this.parent = parent;
	}

	public Object getData(){
		return this.data;
	}
	
	public void setData(Object data){
		this.data = data;
	}

	public List<RoleTreeItemWrapper>  getChildren(){
		return this.children;
	}
	
}
