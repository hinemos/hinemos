/*
Copyright (C) 2010 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.
 */

package com.clustercontrol.nodemap.bean;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.repository.bean.FacilityConstant;

/**
 * 描画に利用するためのスコープ要素。
 * @since 1.0.0
 */
@XmlType(namespace = "http://nodemap.ws.clustercontrol.com")
public class ScopeElement extends FacilityElement {
	private static final long serialVersionUID = 4185973963552875205L;

	public ScopeElement(String parentId, String facilityId, String facilityName, String iconImage, String ownerRoleId, boolean builtin, boolean valid) {
		setParentId(parentId);
		setFacilityId(facilityId);
		setFacilityName(facilityName);
		setOwnerRoleId(ownerRoleId);
		setIconImage(iconImage);
		setBuiltin(builtin);
		// スコープ単位での有効/無効は指定できないため、常に有効
		setValid(valid);
		setTypeName(FacilityConstant.TYPE_SCOPE_STRING);
	}
}
