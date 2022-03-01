/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.InternalManagerError;
import com.clustercontrol.xcloud.Session.SessionScope;
import com.clustercontrol.xcloud.factory.CloudManager;
import com.clustercontrol.xcloud.factory.ICloudOption;
import com.clustercontrol.xcloud.factory.ICloudSpec;

@Entity
@Table(name="cc_cfg_xcloud_platform", schema="setting")
public class CloudPlatformEntity extends EntityBase implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String platformId;
	private String name;
	private String description;

	public CloudPlatformEntity() {
	}
	
	public CloudPlatformEntity(String platformId, String platformName, String description) {
		setPlatformId(platformId);
		setName(platformName);
		setDescription(description);
	}

	public CloudPlatformEntity(CloudPlatformEntity otherEntity)
	{
		setPlatformId(otherEntity.getPlatformId());
		setName(otherEntity.getName());
		setDescription(otherEntity.getDescription());
	}
	
	@Id
	@Column(name="cloud_platform_id")
	public String getPlatformId() {
		return platformId;
	}
	
	public void setPlatformId(String platformId) {
		this.platformId = platformId;
	}
	
	@Column(name="cloud_platform_name")
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Column(name="description")
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getId() {
		return getPlatformId();
	}
	
	public static class CloudSpec implements ICloudSpec {
		private boolean instanceMemo;
		private boolean publicCloud;
		private Class<? extends CredentialBaseEntity> supportedCredential;
		private boolean cloudServiceMonitor;
		private boolean billingAlarm;
		
		public CloudSpec(ICloudSpec cloudSpec) {
			this.instanceMemo = cloudSpec.isInstanceMemoEnabled();
			this.publicCloud = cloudSpec.isPublicCloud();
			this.supportedCredential = cloudSpec.getSupportedCredential();
			this.cloudServiceMonitor = cloudSpec.isCloudServiceMonitorEnabled();
			this.billingAlarm = cloudSpec.isBillingAlarmEnabled();
		}
		
		@Override
		public boolean isPublicCloud() {
			return publicCloud;
		}

		@Override
		public Class<? extends CredentialBaseEntity> getSupportedCredential() {
			return supportedCredential;
		}
		
		@Override
		public boolean isInstanceMemoEnabled() {
			return instanceMemo;
		}
		
		@Override
		public boolean isCloudServiceMonitorEnabled() {
			return cloudServiceMonitor;
		}
		public void setCloudServiceMonitorEnabled(boolean cloudServiceMonitor) {
			this.cloudServiceMonitor = cloudServiceMonitor;
		}
		
		@Override
		public boolean isBillingAlarmEnabled() {
			return billingAlarm;
		}
		public void setBillingAlarmEnabled(boolean billingAlarm) {
			this.billingAlarm = billingAlarm;
		}
	}
	
	public ICloudSpec getCloudSpec() {
		try (SessionScope sessionScope = SessionScope.open()) {
			return CloudManager.singleton().optionCall(getPlatformId(), new CloudManager.OptionCallable<ICloudSpec>() {
				@Override
				public ICloudSpec call(ICloudOption option) throws CloudManagerException {
					return new CloudSpec(option.getCloudSpec());
				}
			});
		} catch (CloudManagerException e) {
			throw new InternalManagerError();
		}
	}
}
