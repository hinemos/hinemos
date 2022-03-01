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

import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.rest.dto.RequestDto;
import com.clustercontrol.rest.endpoint.repository.dto.MapAssociationInfoResponse;

public class RegisterNodeMapModelRequest implements RequestDto {
	private String mapId;
	private String mapName;
	private String mapPath;
	private String description;
	private String parentMapId;
	private String bgName = "default";
	private boolean builtin;
	private String ownerRoleId;

	private HashMap<String, FacilityElementResponse> contents;
	private List<MapAssociationInfoResponse> associations;

	public RegisterNodeMapModelRequest() {
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

	public void addContent(FacilityElementResponse element) {
		this.contents.put(element.getFacilityId(), element);
	}

	public void addAssociation(MapAssociationInfoResponse association) {
		this.associations.add(association);
	}

	public void removeAssociation(MapAssociationInfoResponse association) {
		this.associations.remove(association);
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

	public FacilityElementResponse getElement(String facilityId) {
		FacilityElementResponse element = contents.get(facilityId);
		return element;
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

	@Deprecated
	public void setMapId(String mapId) {
		this.mapId = mapId;
	}

	@Deprecated
	public void setMapName(String mapName) {
		this.mapName = mapName;
	}

	@Deprecated
	public void setMapPath(String mapPath) {
		this.mapPath = mapPath;
	}

	@Deprecated
	public void setContents(HashMap<String, FacilityElementResponse> contents) {
		this.contents = contents;
	}

	@Deprecated
	public void setAssociations(List<MapAssociationInfoResponse> associations) {
		this.associations = associations;
	}

	public HashMap<String, FacilityElementResponse> getContents() {
		return contents;
	}

	public List<MapAssociationInfoResponse> getAssociations() {
		return associations;
	}

	@Override
	public void correlationCheck() throws InvalidSetting {
	}

}
