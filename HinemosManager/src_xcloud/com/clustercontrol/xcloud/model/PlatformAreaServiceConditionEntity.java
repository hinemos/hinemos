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
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * インスタンス情報のBMP Entity Bean クラス<BR>
 */
@NamedQueries({
	@NamedQuery(
			name="findPlatformAreaServiceConditionsByLocationId",
			query="SELECT c FROM PlatformAreaServiceConditionEntity c WHERE c.platformId = :platformId AND c.locationId = :locationId"
			)
})
@Entity
@Table(name="cc_xcloud_platform_area_service_condition", schema="log")
@IdClass(PlatformAreaServiceConditionEntity.PlatformAreaServiceConditionEntityPK.class)
public class PlatformAreaServiceConditionEntity extends ServiceConditionEntity {
	public static class PlatformAreaServiceConditionEntityPK {
		private String platformId;
		private String locationId;
		private String serviceId;
		
		public PlatformAreaServiceConditionEntityPK() {
		}
		
		public PlatformAreaServiceConditionEntityPK(String platformId, String locationId, String serviceId) {
			this.platformId = platformId;
			this.locationId = locationId;
			this.serviceId = serviceId;
		}
		
		public String getPlatformId() {
			return platformId;
		}
		public void setPlatformId(String platformId) {
			this.platformId = platformId;
		}
		
		public String getLocationId() {
			return this.locationId;
		}
		public void setRegion(String locationId) {
			this.locationId = locationId;
		}
		
		public String getServiceId() {
			return serviceId;
		}
		public void setServiceId(String serviceId) {
			this.serviceId = serviceId;
		}
	}
	
	private String platformId;
	private String locationId;
	private CloudPlatformEntity platform;

	public PlatformAreaServiceConditionEntity() {
	}

	@Id
	@Column(name="cloud_platform_id")
	public String getPlatformId() {
		return platformId;
	}
	public void setPlatformId(String platformId) {
		this.platformId = platformId;
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
	@JoinColumn(name="cloud_platform_id", referencedColumnName="cloud_platform_id", insertable=false, updatable=false)
	public CloudPlatformEntity getPlatform() {
		return platform;
	}
	public void setPlatform(CloudPlatformEntity platform) {
		this.platform = platform;
	}
	
	@Override
	public PlatformAreaServiceConditionEntity.PlatformAreaServiceConditionEntityPK getId() {
		return new PlatformAreaServiceConditionEntity.PlatformAreaServiceConditionEntityPK(getPlatformId(), getLocationId(), getServiceName());
	}
}
