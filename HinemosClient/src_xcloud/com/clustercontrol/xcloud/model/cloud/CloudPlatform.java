/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import java.util.ArrayList;
import java.util.List;

import org.openapitools.client.model.CloudPlatformInfoResponse;
import org.openapitools.client.model.CloudSpecResponse;

import com.clustercontrol.xcloud.model.base.Element;

public class CloudPlatform extends Element implements ICloudPlatform {
	private HinemosManager manager;
	
	private String id;
	private String name;
	private String description;
	private CloudSpecResponse cloudSpec;
	
	public CloudPlatform(HinemosManager manager){
		this.manager = manager;
	}
	
	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public CloudSpecResponse getCloudSpec() {
		return cloudSpec;
	}

	public void setId(String id) {
		internalSetProperty(p.id, id, ()->this.id, (s)->this.id=s);
	}

	public void setName(String name) {
		internalSetProperty(p.name, name, ()->this.name, (s)->this.name=s);
	}

	public void setDescription(String description) {
		internalSetProperty(p.description, description, ()->this.description, (s)->this.description=s);
	}

	public void setCloudSpec(CloudSpecResponse cloudSpec) {
		internalSetProperty(p.cloudSpec, cloudSpec, ()->this.cloudSpec, (s)->this.cloudSpec=s);
	}

	@Override
	public List<ICloudScope> getChildCloudScopes() {
		List<ICloudScope> list = new ArrayList<>();
		for(ICloudScope scope: getHinemosManager().getCloudScopes().getCloudScopes()){
			if(id.equals(scope.getPlatformId())){
				list.add(scope);
			}
		}
		return list;
	}
	
	public void update(CloudPlatformInfoResponse platform) {
		setId(platform.getEntity().getPlatformId());
		setName(platform.getEntity().getName());
		setDescription(platform.getEntity().getDescription());
		setCloudSpec(platform.getCloudSpec());
	}
	
	public static CloudPlatform convert(HinemosManager manager, CloudPlatformInfoResponse platform) {
		CloudPlatform p = new CloudPlatform(manager);
		p.update(platform);
		return p;
	}

	@Override
	public IHinemosManager getHinemosManager() {
		return manager;
	}
	
	public boolean equalValues(CloudPlatformInfoResponse source) {
		assert source != null;
		return this.getId().equals(source.getEntity().getPlatformId());
	}
}
