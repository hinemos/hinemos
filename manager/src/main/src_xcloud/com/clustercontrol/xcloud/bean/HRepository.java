/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlList;

public class HRepository {
	private List<CloudPlatform> platforms = new ArrayList<>();
	private List<CloudScope> cloudScopes = new ArrayList<>();
	private List<CloudLoginUser> loginUsers = new ArrayList<>();
	private List<HFacility> facilities = new ArrayList<>();
	private List<HNode> nodes = new ArrayList<>();
	private List<HScope> scopes = new ArrayList<>();
	private List<Instance> instances = new ArrayList<>();
	private List<InstanceBackup> instanceBackups = new ArrayList<>();
	private List<Storage> storages = new ArrayList<>();
	private List<StorageBackup> storageBackups = new ArrayList<>();
	
	@XmlIDREF
	@XmlList
	public List<HFacility> getFacilities() {
		return facilities;
	}
	public void setFacilities(List<HFacility> facilities) {
		this.facilities = facilities;
	}
	
	public List<HScope> getScopes() {
		return scopes;
	}
	public void setScopes(List<HScope> scopes) {
		this.scopes = scopes;
	}
	
	public List<HNode> getNodes() {
		return nodes;
	}
	public void setNodes(List<HNode> nodes) {
		this.nodes = nodes;
	}

	public List<Instance> getInstances() {
		return instances;
	}
	public void setInstances(List<Instance> instances) {
		this.instances = instances;
	}

	public List<Storage> getStorages() {
		return storages;
	}
	public void setStorages(List<Storage> storages) {
		this.storages = storages;
	}
	
	public List<CloudPlatform> getPlatforms() {
		return platforms;
	}
	public void setPlatforms(List<CloudPlatform> platforms) {
		this.platforms = platforms;
	}
	
	public List<CloudScope> getCloudScopes() {
		return cloudScopes;
	}
	public void setCloudScopes(List<CloudScope> cloudScopes) {
		this.cloudScopes = cloudScopes;
	}
	
	public List<CloudLoginUser> getLoginUsers() {
		return loginUsers;
	}
	public void setLoginUsers(List<CloudLoginUser> loginUsers) {
		this.loginUsers = loginUsers;
	}
	
	public List<InstanceBackup> getInstanceBackups() {
		return instanceBackups;
	}
	public void setInstanceBackups(List<InstanceBackup> instanceBackups) {
		this.instanceBackups = instanceBackups;
	}
	
	public List<StorageBackup> getStorageBackups() {
		return storageBackups;
	}
	public void setStorageBackups(List<StorageBackup> storageBackups) {
		this.storageBackups = storageBackups;
	}
}
