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

import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.InternalManagerError;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.PrivateCloudScopeEntity;
import com.clustercontrol.xcloud.model.PublicCloudScopeEntity;
import com.clustercontrol.xcloud.util.FacilityIdUtil;
import com.clustercontrol.xcloud.validation.annotation.ReadOnly;

@XmlRootElement(namespace ="http://xcloud.ws.clustercontrol.com")
@XmlSeeAlso({PublicCloudScope.class, PrivateCloudScope.class})
public abstract class CloudScope implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8363096469399438827L;

	public interface IVisitor {
		void visit(PublicCloudScope scope) throws CloudManagerException;
		void visit(PrivateCloudScope scope) throws CloudManagerException;
	}
	
	public interface ITransformer<T> {
		T transform(PublicCloudScope scope) throws CloudManagerException;
		T transform(PrivateCloudScope scope) throws CloudManagerException;
	}
	
	public String getPlatformId() {
		return getEntity().getPlatformId();
	}
	public void setPlatformId(String platformId) {
		throw new UnsupportedOperationException();
	}

	public String getOwnerRoleId() {
		return getEntity().getOwnerRoleId();
	}
	public void setOwnerRoleId(String accountId) {
		throw new UnsupportedOperationException();
	}

	public String getDescription() {
		return getEntity().getDescription();
	}
	public void setDescription(String description) {
		throw new UnsupportedOperationException();
	}

	public String getId() {
		return getEntity().getId();
	}
	public void setId(String id) {
		throw new UnsupportedOperationException();
	}

	public String getNodeId() {
		return FacilityIdUtil.getCloudScopeNodeId(getEntity().getPlatformId(), getEntity().getId());
	}
	public void setNodeId(String id) {
		throw new UnsupportedOperationException();
	}

	public String getName() {
		return getEntity().getName();
	}
	public void setName(String name) {
		throw new UnsupportedOperationException();
	}

	public String getAccountId() {
		return getEntity().getAccountId();
	}
	public void setAccountId(String accountId) {
		throw new UnsupportedOperationException();
	}

	public boolean isPublic() {
		return getEntity().isPublic();
	}
	public void setPublic(boolean isPublic) {
		throw new UnsupportedOperationException();
	}
	
	public Boolean getBillingDetailCollectorFlg() {
		return getEntity().getBillingDetailCollectorFlg();
	}
	
	public void setBillingDetailCollectorFlg(Boolean billingDetailCollectorFlg) {
		throw new UnsupportedOperationException();
	}

	public Integer getRetentionPeriod() {
		return getEntity().getRetentionPeriod();
	}
	
	public void setRetentionPeriod(Integer retentionPeriod) {
		throw new UnsupportedOperationException();
	}

	public List<Location> getLocations() {
		try {
			return Location.convertWebEntities(getEntity().getLocations());
		} catch (CloudManagerException e) {
			throw new InternalManagerError(e.getMessage(), e);
		}
	}
	public void setLocations(List<Location> locations) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * 登録日を取得します。
	 * 
	 * @return 登録日。
	 */
	@ReadOnly
	public Long getRegDate() {
		return getEntity().getRegDate();
	}

	/**
	 * 登録日を指定します。参照時のみ有効となるので使用しません。
	 * 
	 * @param regDate 登録日。
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
		return getEntity().getUpdateDate();

	}

	/**
	 * 更新日を指定します。参照時のみ有効となるので使用しません。
	 * 
	 * @param updateDate 更新日。
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
		return getEntity().getRegUser();
	}

	/**
	 * 登録ユーザーを指定します。参照時のみ有効となるので使用しません。
	 * 
	 * @param regUser 登録ユーザー。
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
		return getEntity().getUpdateUser();
	}

	/**
	 * 更新ユーザーを指定します。参照時のみ有効となるので使用しません。
	 * 
	 * @param updateUser 更新ユーザー。
	 */
	public void setUpdateUser(String updateUser) {
		throw new UnsupportedOperationException();
	}
	
	public static CloudScope convertWebEntity(CloudScopeEntity cloudScope) throws CloudManagerException {
		return cloudScope.transform(new CloudScopeEntity.ITransformer<CloudScope>() {
			@Override
			public CloudScope transform(PublicCloudScopeEntity scope) throws CloudManagerException {
				return new PublicCloudScope(scope);
			}
			@Override
			public CloudScope transform(PrivateCloudScopeEntity scope) throws CloudManagerException {
				return new PrivateCloudScope(scope);
			}
		});
	}
	
	/**
	 */
	public List<ExtendedProperty> getExtendedProperties() {
		List<ExtendedProperty> properties = new ArrayList<>();
		for (com.clustercontrol.xcloud.model.ExtendedProperty peoperty: getEntity().getExtendedProperties().values()) {
			properties.add(ExtendedProperty.convertWebEntity(peoperty));
		}
		return properties;
	}
	
	/**
	 */
	public void setExtendedProperties(List<ExtendedProperty> properties) {
		throw new UnsupportedOperationException();
	}
	
	@XmlID
	public String getIdentity() {
		return String.valueOf(hashCode());
	}
	public void setIdentity(String identity) {
		throw new UnsupportedOperationException();
	}
	
	@XmlTransient
	public abstract CloudScopeEntity getEntity();
	
	public abstract void visit(IVisitor visitor) throws CloudManagerException;
	
	public abstract <T> T transform(ITransformer<T> transformer) throws CloudManagerException;
}
