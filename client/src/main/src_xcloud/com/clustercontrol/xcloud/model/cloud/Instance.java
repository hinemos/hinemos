/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.clustercontrol.ws.xcloud.CloudEndpoint;
import com.clustercontrol.ws.xcloud.CloudManagerException;
import com.clustercontrol.ws.xcloud.InvalidRole_Exception;
import com.clustercontrol.ws.xcloud.InvalidUserPass_Exception;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.repository.InstanceNode;

public class Instance extends Resource implements IInstance {
    protected String facilityId;
    protected String instanceId;
    protected String status;
    protected String nativeStatus;
    protected List<String> ipAddresses;
    protected String memo;
    protected String name;
    protected String platform;
    protected Long regDate;
    protected String regUser;
    protected List<Tag> tags;
    protected Long updateDate;
    protected String updateUser;
    
    protected InstanceBackup backup;
	
    protected List<InstanceNode> counterNodes = new ArrayList<>();
	
	public Instance() {
		this.backup = new InstanceBackup(this);
	}
	
	@Override
	public String getFacilityId() {return facilityId;}
	public void setFacilityId(String facilityId) {internalSetProperty(IInstance.p.facilityId, facilityId, ()->this.facilityId, (s)->this.facilityId=s);}
	
	@Override
	public String getName() {return name;}
	public void setName(String name) {internalSetProperty(IInstance.p.name, name, ()->this.name, (s)->this.name=s);}
	
	@Override
	public String getId() {return instanceId;}
	public void setId(String instanceId) {this.instanceId = instanceId;}
	
	@Override
	public String getPlatform() {return platform;}
	public void setPlatform(String platform) {internalSetProperty(IInstance.p.platform, platform, ()->this.platform, (s)->this.platform=s);}
	
	@Override
	public Long getRegDate() {return regDate;}
	public void setRegDate(Long regDate) {this.regDate = regDate;}
	
	@Override
	public String getRegUser() {return regUser;}
	public void setRegUser(String regUser) {this.regUser = regUser;}
	
	@Override
	public Long getUpdateDate() {return updateDate;}
	public void setUpdateDate(Long updateDate) {internalSetProperty(IInstance.p.updateDate, updateDate, ()->this.updateDate, (s)->this.updateDate=s);}
	
	@Override
	public String getUpdateUser() {return updateUser;}	
	public void setUpdateUser(String updateUser) {internalSetProperty(IInstance.p.updateUser, updateUser, ()->this.updateUser, (s)->this.updateUser=s);}
	
	@Override
	public ComputeResources getCloudComputeManager() {return (ComputeResources)getOwner();}
	
	public static Instance convert(com.clustercontrol.ws.xcloud.Instance source) {
		Instance instance = new Instance();
		instance.update(source);
		return instance;
	}

	protected void update(com.clustercontrol.ws.xcloud.Instance source) {
		setFacilityId(source.getFacilityId());
		setId(source.getId());
		setStatus(source.getInstanceStatus().value());
		setNativeStatus(source.getInstanceStatusAsPlatform());
		setPlatform(source.getPlatform().name());
		setMemo(source.getMemo());
		setRegDate(source.getRegDate());
		setRegUser(source.getRegUser());
		setUpdateDate(source.getUpdateDate());
		setUpdateUser(source.getUpdateUser());
		setName(source.getName());
		setIpAddresses(source.getIpAddresses());
		List<Tag> tags = new ArrayList<>();
		if(source.getTags() != null){
			for(com.clustercontrol.ws.xcloud.Tag tag: source.getTags()){
				Tag tmpTag = new Tag();
				tmpTag.setKey(tag.getKey());
				tmpTag.setType(tag.getTagType().value());
				tmpTag.setValue(tag.getValue());
				tags.add(tmpTag);
			}
		}
		setTags(tags);
		updateExtendedProperties(source.getExtendedProperties());
	}
	
	public void update() {
		try {
			CloudEndpoint endpoint = getCloudScope().getCloudScopes().getHinemosManager().getEndpoint(CloudEndpoint.class);
			List<com.clustercontrol.ws.xcloud.Instance> instances = endpoint.getInstances(getCloudScope().getId(), getLocation().getId(), Arrays.asList(getId()));
			if (!instances.isEmpty())
				update(instances.get(0));
		} catch (CloudManagerException | InvalidRole_Exception
				| InvalidUserPass_Exception e) {
			throw new CloudModelException(e);
		}
	}
	
	@Override
	public List<Tag> getTags() {return tags;}
	public void setTags(List<Tag> tags) {internalSetProperty(IInstance.p.tags, tags, ()->this.tags, (s)->this.tags=s);}
	
	@Override
	public String getMemo() {return memo;}
	public void setMemo(String memo) {internalSetProperty(IInstance.p.memo, memo, ()->this.memo, (s)->this.memo=s);}

	@Override
	public String getStatus() {return status;}
	public void setStatus(String status) {internalSetProperty(IInstance.p.status, status, ()->this.status, (s)->this.status=s);}
	@Override
	
	public String getNativeStatus() {return nativeStatus;}
	public void setNativeStatus(String nativeStatus) {internalSetProperty(IInstance.p.nativeStatus, nativeStatus, ()->this.nativeStatus, (s)->this.nativeStatus=s);}

	@Override
	public List<String> getIpAddresses() {return ipAddresses;}
	public void setIpAddresses(List<String> ipAddresses) {internalSetProperty(IInstance.p.ipAddresses, ipAddresses, ()->this.ipAddresses, (s)->this.ipAddresses=s);}
	
	public boolean equalValues(com.clustercontrol.ws.xcloud.Instance source) {
		return this.getId().equals(source.getId());
	}

	@Override
	public Location getLocation() {
		return getCloudComputeManager().getLocation();
	}

	@Override
	public InstanceNode[] getCounterNodes() {
		return counterNodes.toArray(new InstanceNode[counterNodes.size()]);
	}
	public void addCounterNode(InstanceNode counterNode) {
		this.counterNodes.add(counterNode);
		counterNode.setInstance(this);
	}
	public void removeCounterNode(InstanceNode counterNode) {
		this.counterNodes.remove(counterNode);
		counterNode.setInstance(null);
	}
	
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (adapter == ICloudScope.class)
			return getCloudScope();
		return super.getAdapter(adapter);
	}

	@Override
	public InstanceBackup getBackup() {
		return backup;
	}
}
