/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.InternalManagerError;
import com.clustercontrol.xcloud.Session.SessionScope;
import com.clustercontrol.xcloud.factory.IPrivateCloudOption;
import com.clustercontrol.xcloud.factory.IPublicCloudOption;
import com.clustercontrol.xcloud.model.CloudScopeEntity.OptionCallableEx;


/**
 * 
 */
@Entity
@Table(name="cc_cfg_xcloud_private_location", schema="setting")
@IdClass(PrivateLocationEntity.PrivateLocationEntityPK.class)
public class PrivateLocationEntity extends EntityBase {
	public static class PrivateLocationEntityPK implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		
		public String cloudScopeId;
		public String locationId;

		public PrivateLocationEntityPK() {
		}

		public PrivateLocationEntityPK(String cloudScopeId, String locationId) {
			this.cloudScopeId = cloudScopeId;
			this.locationId = locationId;
		}

		public String getCloudScopeId() {
			return cloudScopeId;
		}
		public void setCloudScopeId(String cloudScopeId) {
			this.cloudScopeId = cloudScopeId;
		}

		public String getLocationId() {
			return locationId;
		}
		public void setLocationId(String locationId) {
			this.locationId = locationId;
		}
	}
	
	private String cloudScopeId;
	private String locationId;
	private String name;
	
	private List<PrivateEndpointEntity> endpoints = new ArrayList<>();

	public PrivateLocationEntity() {
	}

	public PrivateLocationEntity(String platformId, String locationId, String name) {
		setCloudScopeId(platformId);
		setLocationId(locationId);
		setName(name);
	}

	public PrivateLocationEntity(PrivateLocationEntity otherData) {
		setCloudScopeId(otherData.getCloudScopeId());
		setLocationId(otherData.getLocationId());
		setName(otherData.getName());
	}

	@Id
	@Column(name="cloud_scope_id")
	public String getCloudScopeId() {
		return cloudScopeId;
	}
	public void setCloudScopeId(String cloudScopeId) {
		this.cloudScopeId = cloudScopeId;
	}

	@Id
	@Column(name="location_id")
	public String getLocationId() {
		return locationId;
	}
	public void setLocationId(String locationId) {
		this.locationId = locationId;
	}
	
	@Column(name="location_name")
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@JoinColumns({
		@JoinColumn(name="cloud_scope_id", referencedColumnName="cloud_scope_id", nullable=false),
		@JoinColumn(name="location_id", referencedColumnName="location_id", nullable=false)
	})
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.EAGER, mappedBy="location")
	public List<PrivateEndpointEntity> getEndpoints() {
		return this.endpoints;
	}
	public void setEndpoints(List<PrivateEndpointEntity> endpoints) {
		this.endpoints = endpoints;
	}

	@Override
	public PrivateLocationEntity.PrivateLocationEntityPK getId() {
		return new PrivateLocationEntity.PrivateLocationEntityPK(getCloudScopeId(), getName());
	}
	
	public LocationEntity toLocationEntity(final PrivateCloudScopeEntity cloudScope) {
		try (SessionScope sessionScope = SessionScope.open()) {
			return cloudScope.callOptionEx(new OptionCallableEx<LocationEntity>() {
				@Override
				public LocationEntity call(PublicCloudScopeEntity scope, IPublicCloudOption option) throws CloudManagerException {
					throw new InternalManagerError();
				}
				@Override
				public LocationEntity call(PrivateCloudScopeEntity scope, IPrivateCloudOption option) throws CloudManagerException {
					LocationEntity location = new LocationEntity();
					location.setCloudScope(cloudScope);
					location.setLocationId(PrivateLocationEntity.this.getLocationId());
					location.setName(PrivateLocationEntity.this.getName());
					location.setEntryType(LocationEntity.EntryType.user);
					location.setLocationType(option.getLocationSpec().getLocationType());
					for (PrivateEndpointEntity endpoint: PrivateLocationEntity.this.getEndpoints()) {
						location.getEndpoints().put(endpoint.getEndpointId(), endpoint.toEndpointEntity(location));
					}
					return location;
				}
			});
		} catch(CloudManagerException e) {
			throw new InternalManagerError(e);
		}
	}
}
