/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.nodemap.dto;

import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;

public class FacilityElementResponse {

	private String facilityId;

	@RestPartiallyTransrateTarget
	private String facilityName;

	private String iconImage;

	private String parentId;

	private Integer x = -1;
	private Integer y = -1;

	private String typeName;

	private boolean builtin;

	private boolean valid;

	private boolean newcomer = true;

	private String ownerRoleId;

	NodeMapAttributesResponse attributes = new NodeMapAttributesResponse();

	public FacilityElementResponse() {
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getFacilityName() {
		return facilityName;
	}

	public void setFacilityName(String facilityName) {
		this.facilityName = facilityName;
	}

	public String getIconImage() {
		return iconImage;
	}

	public void setIconImage(String iconImage) {
		this.iconImage = iconImage;
	}

	public Integer getX() {
		return x;
	}

	public Integer getY() {
		return y;
	}

	@Deprecated
	public void setX(Integer x) {
		this.x = x;
	}

	@Deprecated
	public void setY(Integer y) {
		this.y = y;
	}

	public void setPosition(Integer x, Integer y) {
		this.x = x;
		this.y = y;
		this.newcomer = false;
	}

	public String getAttribute(String key) {
		return attributes.getProperty(key, "");
	}

	public void setAttributes(String key, Object obj) {
		if (obj != null) {
			this.attributes.setProperty(key, obj.toString());
		}
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public boolean isBuiltin() {
		return builtin;
	}

	public void setBuiltin(boolean builtin) {
		this.builtin = builtin;
	}

	public String getOwnerRoleId() {
		return ownerRoleId;
	}

	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}

	public boolean isValid() {
		return valid;
	}

	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public boolean isNewcomer() {
		return newcomer;
	}

	public void setNewcomer(boolean newcomer) {
		if (newcomer == true) {
			x = -1;
			y = -1;
		}
		this.newcomer = newcomer;
	}

	@Deprecated
	public NodeMapAttributesResponse getAttributes() {
		return attributes;
	}

	@Deprecated
	public void setAttributes(NodeMapAttributesResponse attributes) {
		this.attributes = attributes;
	}
}
