/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.nodemap.dto;

import java.util.HashMap;
import java.util.List;

import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;
import com.clustercontrol.rest.endpoint.repository.dto.MapAssociationInfoResponse;

public class NodeMapModelResponse {
	private String mapId;
	@RestPartiallyTransrateTarget
	private String mapName;
	@RestPartiallyTransrateTarget
	private String mapPath;
	private String description;
	private String parentMapId;
	private String bgName = "default";
	private boolean builtin;
	private String ownerRoleId;

	private HashMap<String, FacilityElementResponse> contents;
	private List<MapAssociationInfoResponse> associations;

	public NodeMapModelResponse() {
	}

	public String getMapId() {
		return mapId;
	}

	public String getParentMapId() {
		return parentMapId;
	}

	public void setParentMapId(String parentMapId) {
		this.parentMapId = parentMapId;
	}

	public String getMapName() {
		return mapName;
	}

	public String getMapPath() {
		return mapPath;
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

	public String getBgName() {
		return bgName;
	}

	public void setBgName(String bgName) {
		this.bgName = bgName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setMapId(String mapId) {
		this.mapId = mapId;
	}

	public void setMapName(String mapName) {
		this.mapName = mapName;
	}

	public void setMapPath(String mapPath) {
		this.mapPath = mapPath;
	}

	public void setContents(HashMap<String, FacilityElementResponse> contents) {
		this.contents = contents;
	}

	public void setAssociations(List<MapAssociationInfoResponse> associations) {
		this.associations = associations;
	}

	public HashMap<String, FacilityElementResponse> getContents() {
		return contents;
	}

	public List<MapAssociationInfoResponse> getAssociations() {
		return associations;
	}

}
