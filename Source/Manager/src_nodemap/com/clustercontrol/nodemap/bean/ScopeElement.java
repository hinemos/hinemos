/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
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
