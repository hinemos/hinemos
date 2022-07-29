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
import com.clustercontrol.xcloud.factory.ICloudSpec;
import com.clustercontrol.xcloud.model.CloudPlatformEntity;

@XmlRootElement(namespace ="http://xcloud.ws.clustercontrol.com") 
public class CloudPlatform implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2956412014219942832L;

	private CloudPlatformEntity entity;

	public CloudPlatform() {
		entity = new CloudPlatformEntity();
	}
	
	public CloudPlatform(CloudPlatformEntity entity) {
		this.entity = entity;
	}
	
	public String getId() {
		return entity.getPlatformId();
	}
	public void setId(String platformId) {
		throw new UnsupportedOperationException();
	}
	
	public String getName() {
		return entity.getName();
	}
	public void setName(String name) {
		throw new UnsupportedOperationException();
	}
	
	public String getDescription() {
		return entity.getDescription();
	}
	public void setDescription(String description) {
		throw new UnsupportedOperationException();
	}

	public CloudSpec getCloudSpec() {
		ICloudSpec spec = entity.getCloudSpec();
		CloudSpec cloudspec = new CloudSpec();
		cloudspec.setBillingAlarmEnabled(spec.isBillingAlarmEnabled());
		cloudspec.setCloudServiceMonitorEnabled(spec.isCloudServiceMonitorEnabled());
		cloudspec.setPublicCloud(spec.isPublicCloud());
		return cloudspec;
	}
	public void setCloudSpec(CloudSpec cloudSpec) {
		throw new UnsupportedOperationException();
	}
	
	@XmlTransient
	public CloudPlatformEntity getEntity() {
		return entity;
	}
	
	public static CloudPlatform convertWebEntity(CloudPlatformEntity platformEntity) throws CloudManagerException {
		return new CloudPlatform(platformEntity);
	}
	
	public static List<CloudPlatform> convertWebEntities(List<CloudPlatformEntity> platformEntities) throws CloudManagerException {
		List<CloudPlatform> list = new ArrayList<>();
		for (CloudPlatformEntity p: platformEntities) {
			list.add(CloudPlatform.convertWebEntity(p));
		}
		return list;
	}
}
