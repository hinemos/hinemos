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

import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.factory.IResourceManagement.Instance.Platform;
import com.clustercontrol.xcloud.model.InstanceEntity;

public class Instance extends LocationResource {
	private InstanceEntity entity;

	public Instance() {
	}

	public Instance(InstanceEntity entity) {
		setCloudScopeId(entity.getCloudScopeId());
		setLocationId(entity.getLocationId());
		setId(entity.getResourceId());
		setName(entity.getName());
		setResourceType(ResourceType.Instance);
		setResourceTypeAsPlatform(entity.getResourceTypeAsPlatform());
		
		List<ExtendedProperty> properties = new ArrayList<>();
		for (com.clustercontrol.xcloud.model.ExtendedProperty peoperty: entity.getExtendedProperties().values()) {
			properties.add(ExtendedProperty.convertWebEntity(peoperty));
		}
		setExtendedProperties(properties);
		
		this.entity = entity;
	}
	
	/**
	 * インスタンスに紐づいたノードのファシリティ ID を取得する。
	 * 
	 * @return ファシリティ ID。
	 */
	public String getFacilityId() {
		return entity.getFacilityId();
	}
	/**
	 * 使用禁止。
	 * 
	 * @param facilityId
	 */
	public void setFacilityId(String facilityId) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * インスタンス ＩＤ を取得する。
	 * 
	 * @return インスタンス ＩＤ。
	 */
	public Platform getPlatform() {
		return entity.getPlatform();
	}
	/**
	 * 使用禁止。
	 * 
	 * @param instanceId
	 */
	public void setPlatform(Platform platform) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * インスタンスの状態を取得する。
	 * 
	 * @return インスタンスの状態。
	 */
	public InstanceStatus getInstanceStatus() {
		return entity.getInstanceStatus();
	}
	/**
	 * 使用禁止。
	 * 
	 * @param state
	 */
	public void setInstanceStatus(InstanceStatus state) {
		throw new UnsupportedOperationException();
	}

	/**
	 * インスタンスの状態を取得する。
	 * 
	 * @return インスタンスの状態。
	 */
	public String getInstanceStatusAsPlatform() {
		return entity.getInstanceStatusAsPlatform();
	}
	/**
	 * 使用禁止。
	 * 
	 * @param state
	 */
	public void setInstanceStatusAsPlatform(String status) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * インスタンスの状態を取得する。
	 * 
	 * @return インスタンスの状態。
	 */
	public List<String> getIpAddresses() {
		return entity.getIpAddresses();
	}
	/**
	 * 使用禁止。
	 * 
	 * @param state
	 */
	public void setIpAddresses(List<String> ipAddresses) {
		throw new UnsupportedOperationException();
	}
	
	public String getMemo() {
		return entity.getMemo();
	}
	public void setMemo(String memo) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * インスタンスに紐づいたタグの一覧を取得する。
	 * 
	 * @return タグの一覧。
	 */
	public List<Tag> getTags() {
		return Tag.convertWebEntities(new ArrayList<>(entity.getTags().values()));
	}
	/**
	 * 使用禁止。
	 * 
	 * @param tags
	 */
	public void setTags(List<Tag> tags) {
		throw new UnsupportedOperationException();
	}

	/**
	 * この情報を登録した日時を取得する。
	 * 
	 * @return この情報を登録した日時。
	 */
	public Long getRegDate() {
		return entity.getRegDate();
	}
	/**
	 * 未使用。
	 * 
	 * @param regDate
	 */
	public void setRegDate(Long regDate) {
		throw new UnsupportedOperationException();
	}

	/**
	 * この情報を更新した日時を取得する。
	 * 
	 * @return この情報を更新した日時。
	 */
	public Long getUpdateDate() {
		return entity.getUpdateDate();
	}
	/**
	 * 未使用。
	 * 
	 * @param updateDate
	 */
	public void setUpdateDate(Long updateDate) {
		throw new UnsupportedOperationException();
	}

	/**
	 * この情報を登録した Hinemos ユーザーを取得する。
	 * 
	 * @return この情報を登録した Hinemos ユーザー。
	 */
	public String getRegUser() {
		return entity.getRegUser();
	}
	/**
	 * 未使用。
	 * 
	 * @param regUser
	 */
	public void setRegUser(String regUser) {
		throw new UnsupportedOperationException();
	}

	/**
	 * この情報を更新した Hinemos ユーザーを取得する。
	 * 
	 * @return この情報を更新した Hinemos ユーザー。
	 */
	public String getUpdateUser() {
		return entity.getUpdateUser();
	}
	/**
	 * 未使用。
	 * 
	 * @param updateUser
	 */
	public void setUpdateUser(String updateUser) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public void visit(IVisitor visitor) throws CloudManagerException {
		visitor.visit(this);
	}
	
	@Override
	public <T> T transform(ITransformer<T> transformer) throws CloudManagerException {
		return transformer.transform(this);
	}
	
	public static Instance convertWebEntity(InstanceEntity entity) {
		return new Instance(entity);
	}

	public static List<Instance> convertWebEntities(List<InstanceEntity> entities) {
		List<Instance> instances = new ArrayList<>();
		for (InstanceEntity entity: entities) {
			instances.add(convertWebEntity(entity));
		}
		return instances;
	}
}
