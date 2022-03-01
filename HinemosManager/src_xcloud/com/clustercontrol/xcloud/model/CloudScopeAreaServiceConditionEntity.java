/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * インスタンス情報のBMP Entity Bean クラス<BR>
 */
@NamedQueries({
	@NamedQuery(
			name="findCloudScopeAreaServiceConditionsByScopeId",
			query="SELECT c FROM CloudScopeAreaServiceConditionEntity c WHERE c.cloudScopeId = :cloudScopeId AND c.locationId = null"
			),
	@NamedQuery(
			name="findCloudScopeAreaServiceConditionsByLocationId",
			query="SELECT c FROM CloudScopeAreaServiceConditionEntity c WHERE c.cloudScopeId = :cloudScopeId AND c.locationId = :locationId"
			)
})
@Entity
@Table(name="cc_xcloud_cloudscope_area_service_condition", schema="log")
@IdClass(CloudScopeAreaServiceConditionEntity.CloudScopeAreaServiceConditionEntityPK.class)
public class CloudScopeAreaServiceConditionEntity extends ServiceConditionEntity {
	public static class CloudScopeAreaServiceConditionEntityPK {
		private String cloudScopeId;
		private String locationId;
		private String serviceId;
		
		public CloudScopeAreaServiceConditionEntityPK() {
		}
		
		public CloudScopeAreaServiceConditionEntityPK(String cloudScopeId, String locationId, String serviceId) {
			this.cloudScopeId = cloudScopeId;
			this.locationId = locationId;
			this.serviceId = serviceId;
		}
		
		public String getCloudScopeId() {
			return cloudScopeId;
		}
		public void setCloudScopeId(String cloudScopeId) {
			this.cloudScopeId = cloudScopeId;
		}
		
		public String getLocationId() {
			return this.locationId;
		}
		public void setLocationId(String locationId) {
			this.locationId = locationId;
		}
		
		public String getServiceId() {
			return serviceId;
		}
		public void setServiceId(String serviceId) {
			this.serviceId = serviceId;
		}
	}
	
	private String cloudScopeId;
	private String locationId;
	private CloudScopeEntity scope;
	
	public CloudScopeAreaServiceConditionEntity() {
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
	public String getLocationId()
	{
		return this.locationId;
	}
	public void setLocationId(String locationId)
	{
		this.locationId = locationId;
	}
	
	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="cloud_scope_id", referencedColumnName="cloud_scope_id", insertable=false, updatable=false)
	public CloudScopeEntity getScope() {
		return scope;
	}
	public void setScope(CloudScopeEntity scope) {
		this.scope = scope;
	}
	
	@Override
	public CloudScopeAreaServiceConditionEntity.CloudScopeAreaServiceConditionEntityPK getId() {
		return new CloudScopeAreaServiceConditionEntity.CloudScopeAreaServiceConditionEntityPK(getCloudScopeId(), getLocationId(), getServiceName());
	}
}
