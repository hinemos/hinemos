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
 * 描画に利用するためのノードの要素。
 * @since 1.0.0
 */
@XmlType(namespace = "http://nodemap.ws.clustercontrol.com")
public class NodeElement extends FacilityElement {
	private static final long serialVersionUID = 4185973963552875205L;

	public NodeElement(String parentId, String facilityId, String facilityName, String iconImage, String ownerRoleId, boolean builtin, boolean valid) {
		setParentId(parentId);
		setFacilityId(facilityId);
		setFacilityName(facilityName);
		setIconImage(iconImage);
		setOwnerRoleId(ownerRoleId);
		setBuiltin(builtin);
		setValid(valid);
		setTypeName(FacilityConstant.TYPE_NODE_STRING);
	}
}
