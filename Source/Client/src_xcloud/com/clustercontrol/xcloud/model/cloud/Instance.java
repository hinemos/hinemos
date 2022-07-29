/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model.cloud;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.InstanceInfoResponse;
import org.openapitools.client.model.ResourceTagResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.util.TimezoneUtil;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.repository.InstanceNode;
import com.clustercontrol.xcloud.util.CloudRestClientWrapper;

public class Instance extends Resource implements IInstance {
	private static Log m_log = LogFactory.getLog(Instance.class);

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
	
	public static Instance convert(InstanceInfoResponse source) {
		Instance instance = new Instance();
		instance.update(source);
		return instance;
	}

	protected void update(InstanceInfoResponse source) {
		setFacilityId(source.getEntity().getFacilityId());
		setId(source.getId());
		setStatus(source.getEntity().getInstanceStatus().getValue());
		setNativeStatus(source.getEntity().getInstanceStatusAsPlatform());
		setPlatform(source.getEntity().getPlatform().getValue());
		setMemo(source.getEntity().getMemo());
		try {
			setRegDate(TimezoneUtil.getSimpleDateFormat().parse(source.getEntity().getRegDate()).getTime());
		} catch (ParseException e) {
			// ここには入らない想定
			m_log.warn("invalid regTime.", e);
		}
		setRegUser(source.getEntity().getRegUser());
		try {
			setUpdateDate(TimezoneUtil.getSimpleDateFormat().parse(source.getEntity().getUpdateDate()).getTime());
		} catch (ParseException e) {
			// ここには入らない想定
			m_log.warn("invalid updateTime.", e);
		}
		setUpdateUser(source.getEntity().getUpdateUser());
		setName(source.getName());
		setIpAddresses(source.getEntity().getIpAddresses());
		List<Tag> tags = new ArrayList<>();
		if(source.getEntity().getTags() != null){
			for(ResourceTagResponse tag: source.getEntity().getTags().values()){
				Tag tmpTag = new Tag();
				tmpTag.setKey(tag.getKey());
				tmpTag.setType(tag.getTagType().getValue());
				tmpTag.setValue(tag.getValue());
				tags.add(tmpTag);
			}
		}
		setTags(tags);
		updateExtendedProperties(source.getExtendedProperties());
	}
	
	public void update() {
		try {
			CloudRestClientWrapper wrapper = getCloudScope().getCloudScopes().getHinemosManager().getWrapper();
			List<InstanceInfoResponse> instances = wrapper.getInstances(getCloudScope().getId(), getLocation().getId(), getId());
			if (!instances.isEmpty())
				update(instances.get(0));
		} catch (CloudManagerException | InvalidUserPass | InvalidRole | RestConnectFailed | HinemosUnknown e) {
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
	
	public boolean equalValues(InstanceInfoResponse source) {
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
