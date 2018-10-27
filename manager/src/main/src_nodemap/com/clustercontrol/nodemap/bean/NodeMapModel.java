/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

/**
 * 描画に利用するためのノードマップモデル。
 * @since 1.0.0
 */
@XmlType(namespace = "http://nodemap.ws.clustercontrol.com")
public class NodeMapModel implements Serializable{
	private static final long serialVersionUID = -6891215951167852724L;

	// Webサービスのため、finalははずしておく
	private String mapId;
	private String mapName;
	private String mapPath;
	private String description;
	private String parentMapId;
	private String bgName = "default";
	private boolean builtin;
	private String ownerRoleId;

	private HashMap<String, FacilityElement> contents;
	private List<Association> associations;

	/*
	 * 引数なしコンストラクタ
	 */
	public NodeMapModel(){
		super();
		this.mapId = "";
		this.mapName = "";
		this.mapPath = "";
	}

	public NodeMapModel(String parentMapId, String mapId, String mapName, String mapPath, String ownerRoleId, boolean builtin){
		this.contents = new HashMap<String, FacilityElement>();
		this.associations = new ArrayList<Association>();
		this.parentMapId = parentMapId;
		this.mapId = mapId;
		this.mapName = mapName;
		this.mapPath = mapPath;
		this.ownerRoleId = ownerRoleId;
		this.builtin = builtin;
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

	public FacilityElement[] getContentArray(){
		return contents.values().toArray(new FacilityElement[contents.size()]);
	}

	public void addContent(FacilityElement element){
		this.contents.put(element.getFacilityId(), element);
	}

	public void addAssociation(Association association){
		this.associations.add(association);
	}

	public void removeAssociation(Association association){
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

	// 登録済みおよび未登録エレメントの中から指定のファシリティIDのエレメントを返します。
	public FacilityElement getElement(String facilityId){
		FacilityElement element = contents.get(facilityId);
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

	// Webサービスのため、setterを用意しておく
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
	public void setContents(HashMap<String, FacilityElement> contents) {
		this.contents = contents;
	}

	@Deprecated
	public void setAssociations(List<Association> associations) {
		this.associations = associations;
	}

	// Webサービスのため、getterを用意しておく
	public HashMap<String, FacilityElement> getContents() {
		return contents;
	}

	public List<Association> getAssociations() {
		return associations;
	}
}
