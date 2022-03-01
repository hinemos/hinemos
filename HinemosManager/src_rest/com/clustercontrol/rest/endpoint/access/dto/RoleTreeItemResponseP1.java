/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.access.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.rest.endpoint.access.dto.enumtype.RoleTreeDataTypeEnum;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertEnum;
import com.clustercontrol.rest.annotation.msgconverter.RestAllTransrateTarget;

public class RoleTreeItemResponseP1 {

	public RoleTreeItemResponseP1() {
	}

	private String id;
	@RestAllTransrateTarget
	private String name;
	@RestBeanConvertEnum
	private RoleTreeDataTypeEnum type;
	private List<RoleTreeItemResponseP1> children = new ArrayList<RoleTreeItemResponseP1>();

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public RoleTreeDataTypeEnum getType() {
		return type;
	}

	public void setType(RoleTreeDataTypeEnum type) {
		this.type = type;
	}

	public List<RoleTreeItemResponseP1> getChildren() {
		return children;
	}

	public void setChildren(List<RoleTreeItemResponseP1> children) {
		this.children = children;
	}

	@Override
	public String toString() {
		return "RoleTreeItemResponseP1 [id=" + id + ", name=" + name + ", type=" + type + ", children=" + children
				+ "]";
	}

}
