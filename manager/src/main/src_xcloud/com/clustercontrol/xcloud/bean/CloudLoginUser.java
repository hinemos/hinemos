/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.model.CloudLoginUserEntity;
import com.clustercontrol.xcloud.model.RoleRelationEntity;
import com.clustercontrol.xcloud.validation.annotation.ReadOnly;

/**
 * クラウドユーザー情報を保持するクラス。 
 * {@link com.clustercontrol.ws.cloud.CloudEndpoint#addCloudUser(CreateCloudUserRequest) addCloudUser 関数}、
 * {@link com.clustercontrol.ws.cloud.CloudEndpoint#modifyCloudUser(ModifyCloudUserRequest) modifyCloudUser 関数} にて使用される。
 *
 */
@XmlRootElement(namespace ="http://xcloud.ws.clustercontrol.com")
public class CloudLoginUser implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -9161813048730684001L;
	
	public static enum CloudUserType {
		account,
		user
	}
	
	private CloudLoginUserEntity entity;

	public CloudLoginUser() {
		this.entity = new CloudLoginUserEntity();
	}

	public CloudLoginUser(CloudLoginUserEntity entity) {
		this.entity = entity;
	}

	/**
	 * クラウド Id を取得します。現在は、"AWS" のみです。
	 * 
	 * @return クラウド Id
	 */
	public String getCloudScopeId() {
		return entity.getCloudScopeId();
	}
	/**
	 * 使用禁止。
	 * 
	 * @param accountResourceId
	 */
	public void setCloudScopeId(String cloudScopeId) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 */
	public String getId() {
		return entity.getLoginUserId();
	}
	/**
	 * 使用禁止。
	 * 
	 * @param accountResourceId
	 */
	public void setId(String id) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * クラウドユーザー名を取得します。
	 * 
	 * @return クラウドユーザー名。
	 */
	public String getName() {
		return entity.getName();
	}
	/**
	 * 使用禁止。
	 * 
	 * @param cloudUserName
	 */
	public void setName(String name) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * 説明を取得します。
	 * 
	 * @return　説明。
	 */
	public String getDescription() {
		return entity.getDescription();
	}
	/**
	 * 使用禁止。
	 * 
	 * @param description
	 */
	public void setDescription(String description) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * クラウドユーザー種別を取得します。
	 * 
	 * @return クラウドユーザー種別。
	 */
	public Credential getCredential() {
		return entity.getCredential().convertWebElement();
	}
	/**
	 * 使用禁止。
	 * 
	 * @param cloudUserType
	 */
	public void setCredential(Credential credential) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * クラウドユーザー種別を取得します。
	 * 
	 * @return クラウドユーザー種別。
	 */
	public CloudUserType getCloudUserType() {
		if (entity.getCloudUserType() != null) {
			return CloudUserType.valueOf(entity.getCloudUserType().name());
		}
		return null;
	}
	/**
	 * 使用禁止。
	 * 
	 * @param cloudUserType
	 */
	public void setCloudUserType(CloudUserType cloudUserType) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * クラウドユーザーに紐づく Hinemos ユーザーを取得します。
	 * 
	 * @return Hinemos ユーザー。
	 */
	public List<RoleRelation> getRoleRelations() {
		List<RoleRelation> relations = new ArrayList<>();
		for (RoleRelationEntity relation: entity.getRoleRelations()) {
			relations.add(new RoleRelation(relation));
		}
		return relations;
	}
	/**
	 * 使用禁止。
	 * 
	 * @param userId
	 */
	public void setRoleRelations(List<RoleRelation> relation) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 */
	public Integer getPriority() {
		return getEntity().getPriority();
	}
	/**
	 * 使用禁止。
	 * 
	 * @param userId
	 */
	public void setPriority(Integer priority) {
		throw new UnsupportedOperationException();
	}

	/**
	 * 登録日を取得します。
	 * 
	 * @return 登録日。
	 */
	@ReadOnly
	public Long getRegDate() {
		return entity.getRegDate();
	}

	/**
	 * 使用禁止。
	 * 
	 * @param regDate
	 */
	public void setRegDate(Long regDate) {
		throw new UnsupportedOperationException();
	}

	/**
	 * 更新日を取得します。
	 * 
	 * @return 更新日。
	 */
	@ReadOnly
	public Long getUpdateDate() {
		return entity.getUpdateDate();

	}

	/**
	 * 使用禁止。
	 * 
	 * @param updateDate
	 */
	public void setUpdateDate(Long updateDate) {
		throw new UnsupportedOperationException();
	}
	/**
	 * 登録ユーザーを取得します。
	 * 
	 * @return 登録ユーザー。
	 */
	@ReadOnly
	public String getRegUser() {
		return entity.getRegUser();
	}

	/**
	 * 使用禁止。
	 * 
	 * @param regUser
	 */
	public void setRegUser(String regUser) {
		throw new UnsupportedOperationException();
	}

	/**
	 * 更新ユーザーを取得します。
	 * 
	 * @return 更新ユーザー。
	 */
	@ReadOnly
	public String getUpdateUser() {
		return entity.getUpdateUser();
	}

	/**
	 * 使用禁止。
	 * 
	 * @param updateUser
	 */
	public void setUpdateUser(String updateUser) {
		throw new UnsupportedOperationException();
	}

	@XmlTransient
	public CloudLoginUserEntity getEntity() {
		return entity;
	}
	
	public static CloudLoginUser convertWebEntity(CloudLoginUserEntity entity) throws CloudManagerException {
		return new CloudLoginUser(entity);
	}
	
	public static List<CloudLoginUser> convertWebEntities(List<CloudLoginUserEntity> entities) throws CloudManagerException {
		List<CloudLoginUser> list = new ArrayList<>();
		for (CloudLoginUserEntity e: entities) {
			list.add(CloudLoginUser.convertWebEntity(e));
		}
		return list;
	}
}
