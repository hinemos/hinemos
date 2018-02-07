/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.clustercontrol.xcloud.persistence.IDHolder;

/**
 * インスタンス情報のBMP Entity Bean クラス<BR>
 */
@Entity
@Table(name="cc_cfg_xcloud_private_endpoint", schema="setting")
@IdClass(PrivateEndpointEntity.ReservedEndpointEntityPK.class)
public class PrivateEndpointEntity implements IDHolder {
	public static class ReservedEndpointEntityPK implements java.io.Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 682905935857533293L;

		public String cloudScopeId;
		public String locationId;
		public String endpointId;

		public ReservedEndpointEntityPK() {
		}

		public ReservedEndpointEntityPK(String cloudScopeId, String locationId, String endpointId) {
			this.cloudScopeId = cloudScopeId;
			this.locationId = locationId;
			this.endpointId = endpointId;
		}

		public String getPlatfromId() {
			return locationId;
		}
		public void setPlatformId(String platformId) {
			this.locationId = platformId;
		}

		public String getLocationId() {
			return locationId;
		}
		public void setLocationId(String locationId) {
			this.locationId = locationId;
		}

		public String getEndpointId() {
			return endpointId;
		}
		public void setEndpointId(String endpointId) {
			this.endpointId = endpointId;
		}
	}
	
	private String cloudScopeId;
	private String locationId;
	private String endpointId;
	private String url;

	public PrivateLocationEntity location;

	public PrivateEndpointEntity()	{
	}

	public PrivateEndpointEntity(String cloudScopeId, String locationId, String type, String url) {
		setCloudScopeId(cloudScopeId);
		setLocationId(locationId);
		setEndpointId(type);
		setUrl(url);
	}

	public PrivateEndpointEntity(PrivateEndpointEntity otherData) {
		setCloudScopeId(otherData.getCloudScopeId());
		setLocationId(otherData.getLocationId());
		setEndpointId(otherData.getEndpointId());
		setUrl(otherData.getUrl());
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

	@Id
	@Column(name="endpoint_id")
	public String getEndpointId() {
		return endpointId;
	}
	public void setEndpointId(String endpointId) {
		this.endpointId = endpointId;
	}
	
	@Column(name="url")
	public String getUrl()
	{
		return this.url;
	}
	public void setUrl( String url )
	{
		this.url = url;
	}
	
	@JoinColumns (
	    {
	    	@JoinColumn(name="cloud_scope_id", referencedColumnName="cloud_scope_id", insertable=false, updatable=false),
	    	@JoinColumn(name="location_id", referencedColumnName="location_id", insertable=false, updatable=false)
	    }
	)
	@ManyToOne
	public PrivateLocationEntity getLocation() {
		return location;
	}
	public void setLocation(PrivateLocationEntity location) {
		this.location = location;
	}

	@Override
	public PrivateEndpointEntity.ReservedEndpointEntityPK getId() {
		return new PrivateEndpointEntity.ReservedEndpointEntityPK(getCloudScopeId(), getLocationId(), getEndpointId());
	}

	public EndpointEntity toEndpointEntity(LocationEntity location) {
		EndpointEntity endpoint = new EndpointEntity();
		endpoint.setLocation(location);
		endpoint.setEndpoint(this.getEndpointId());
		endpoint.setUrl(this.getUrl());
		return endpoint;
	}
}
