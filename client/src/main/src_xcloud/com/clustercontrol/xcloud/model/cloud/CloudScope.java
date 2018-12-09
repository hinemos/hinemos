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

import com.clustercontrol.ws.xcloud.CloudEndpoint;
import com.clustercontrol.ws.xcloud.CloudManagerException;
import com.clustercontrol.ws.xcloud.InvalidRole_Exception;
import com.clustercontrol.ws.xcloud.InvalidUserPass_Exception;
import com.clustercontrol.ws.xcloud.PlatformServiceCondition;
import com.clustercontrol.ws.xcloud.PlatformUser;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.base.Element;
import com.clustercontrol.xcloud.model.repository.ICloudScopeScope;
import com.clustercontrol.xcloud.util.CollectionComparator;

public class CloudScope extends Element implements ICloudScope {
	private LoginUsers loginUsers;

	private List<Location> locations = new ArrayList<>();
	
	private List<ServiceCondition> serviceConditions;

	private String accountId;
	private String id;
	private String name;
	private String platformId;
	private String ownerRoleId;
	private String description;
	private String nodeId;
	private Long regDate;
	private String regUser;
	private Long updateDate;
	private String updateUser;
	private Boolean openness;
	private Integer retentionPeriod;
	private Boolean billingDetailCollectorFlg;
	
	private ICloudScopeScope counterScope;
	
	private CloudScopes cloudScopes;
	
	private List<ExtendedProperty> extendedProperties = new ArrayList<>();
	
	public static CloudScope convert(CloudScopes cloudScopes, com.clustercontrol.ws.xcloud.CloudScope source) {
		CloudScope cloudScope = new CloudScope(cloudScopes);
		cloudScope.update(source);
		return cloudScope;
	}
	
	public CloudScope(CloudScopes cloudScopes) {
		this.cloudScopes = cloudScopes;
	}

	@Override
	public String getAccountId() {return accountId;}

	@Override
	public String getId() {return id;}

	@Override
	public String getName() {return name;}

	@Override
	public String getPlatformId() {return platformId;}

	@Override
	public String getDescription() {return description;}

	@Override
	public Long getRegDate() {return regDate;}

	@Override
	public String getRegUser() {return regUser;}

	@Override
	public Long getUpdateDate() {return updateDate;}

	@Override
	public String getUpdateUser() {return updateUser;}

	@Override
	public boolean isPublic() {return openness;}

	@Override
	public LoginUsers getLoginUsers() {
		if (loginUsers == null){
			loginUsers = new LoginUsers(this);
		}
		return loginUsers;
	}

	public void setAccountId(String accountId) {this.accountId = accountId;}
	
	public void setId(String id) {this.id = id;}

	public void setName(String name) {internalSetProperty(p.name, name, ()->this.name, (s)->this.name=s);}

	public void setPlatformId(String platformId) {this.platformId = platformId;}

	public void setDescription(String description) {internalSetProperty(p.description, description, ()->this.description, (s)->this.description=s);}

	public void setRegDate(Long regDate) {this.regDate = regDate;}

	public void setRegUser(String regUser) {this.regUser = regUser;}

	public void setUpdateDate(Long updateDate) {internalSetProperty(p.updateDate, updateDate, ()->this.updateDate, (s)->this.updateDate=s);}

	public void setUpdateUser(String updateUser) {internalSetProperty(p.updateUser, updateUser, ()->this.updateUser, (s)->this.updateUser=s);}

	public void setPublic(Boolean openness) {internalSetProperty(p.openness, openness, ()->this.openness, (s)->this.openness=s);}
	
	@Override
	public Integer getRetentionPeriod() {
		return retentionPeriod;
	}
	public void setRetentionPeriod(Integer retentionPeriod) {
		internalSetProperty(p.retentionPeriod, retentionPeriod, ()->this.retentionPeriod, (s)->this.retentionPeriod=s);
	}

	@Override
	public Boolean getBillingDetailCollectorFlg() {
		return billingDetailCollectorFlg;
	}
	public void setbillingDetailCollectorFlg(Boolean billingDetailCollectorFlg) {
		internalSetProperty(p.billingDetailCollectorFlg, billingDetailCollectorFlg, ()->this.billingDetailCollectorFlg, (s)->this.billingDetailCollectorFlg=s);
	}
	
	@Override
	public Location[] getLocations() {
		return locations.toArray(new Location[locations.size()]);
	}

	public void update(com.clustercontrol.ws.xcloud.CloudScope source) {
		setAccountId(source.getAccountId());
		setId(source.getId());
		setName(source.getName());
		setDescription(source.getDescription());
		setPlatformId(source.getPlatformId());
		setRegDate(source.getRegDate());
		setRegUser(source.getRegUser());
		setUpdateDate(source.getUpdateDate());
		setUpdateUser(source.getUpdateUser());
		setPublic(source.isPublic());
		setUpdateUser(source.getUpdateUser());
		setPublic(source.isPublic());
		setNodeId(source.getNodeId());
		setOwnerRoleId(source.getOwnerRoleId());
		setRetentionPeriod(source.getRetentionPeriod());
		setbillingDetailCollectorFlg(source.isBillingDetailCollectorFlg());
		
		updateLocations(source.getLocations());
		
		updateExtendedProperties(source.getExtendedProperties());
	}
	
	public void updateLocations(List<com.clustercontrol.ws.xcloud.Location> webLocations){
		CollectionComparator.compareCollection(locations, webLocations, new CollectionComparator.Comparator<Location, com.clustercontrol.ws.xcloud.Location>() {
			public boolean match(Location o1, com.clustercontrol.ws.xcloud.Location o2) {return o1.equalValues(o2);}
			public void matched(Location o1, com.clustercontrol.ws.xcloud.Location o2) {
				o1.update(o2);
			}
			public void afterO1(Location o1) {
				internalRemoveProperty(p.locations, o1, locations);
			}
			public void afterO2(com.clustercontrol.ws.xcloud.Location o2) {
				Location newLocation = Location.convert(CloudScope.this, o2);
				internalAddProperty(p.locations, newLocation, locations);
			}
		});
	}
	
	public boolean equalValues(com.clustercontrol.ws.xcloud.CloudScope source) {
		assert source != null;
		return this.getId().equals(source.getId());
	}

	public CloudEndpoint getEndpoint(){
		return getEndpoint(CloudEndpoint.class);
	}

	@Override
	public Location getLocation(String locationId) {
		for (Location location: getLocations()) {
			if (location.getId().equals(locationId)) {
				return location;
			}
		}
		throw new CloudModelException(String.format("Not found Location of %s", locationId));
	}

	@Override
	public CloudPlatform getCloudPlatform() {
		for(CloudPlatform platform: getCloudScopes().getHinemosManager().getCloudPlatforms()){
			if(platform.getId().equals(getPlatformId())){
				return platform;
			}
		}
		throw new CloudModelException(String.format("Not found platform of %s", platformId));
	}
	
	private <T> T getEndpoint(Class<T> endpointClass){
		return getCloudScopes().getHinemosManager().getEndpoint(endpointClass);
	}
	
	public List<PlatformUser> getUnassignedUsers(){
		try {
			return getEndpoint(CloudEndpoint.class).getAvailablePlatformUsers(id);
		} catch (CloudManagerException | InvalidRole_Exception | InvalidUserPass_Exception e) {
			throw new CloudModelException(e);
 		}
	}

	@Override
	public CloudScopes getCloudScopes() {
		return cloudScopes;
	}

	@Override
	public ICloudScopeScope getCounterScope() {
		return counterScope;
	}
	public void setCounterScope(ICloudScopeScope counterScope) {
		this.counterScope = counterScope;
	}

	@Override
	public ServiceCondition[] getServiceConditions() {
		if (serviceConditions == null)
			return new ServiceCondition[]{};
		return serviceConditions.toArray(new ServiceCondition[serviceConditions.size()]);
	}
	
	@Override
	public IServiceCondition[] getServiceConditionsWithInitializing() {
		if (serviceConditions == null) {
			try {
				serviceConditions = new ArrayList<>();
				CloudEndpoint endpoint = getCloudScopes().getHinemosManager().getEndpoint(CloudEndpoint.class);
				
				List<PlatformServiceCondition> conditions = endpoint.getPlatformServiceConditions(getId());
				for (PlatformServiceCondition condition: conditions) {
					internalAddProperty(p.serviceConditions, ServiceCondition.convert(condition), serviceConditions);
				}
			} catch (CloudManagerException | InvalidRole_Exception | InvalidUserPass_Exception e) {
				throw new CloudModelException(e);
			}
		}
		return serviceConditions.toArray(new ServiceCondition[serviceConditions.size()]);
	}
	
	@Override
	public void updateServiceConditions() {
		if (serviceConditions == null) {
			getServiceConditionsWithInitializing();
			return;
		} else {
			try {
				CloudEndpoint endpoint = getCloudScopes().getHinemosManager().getEndpoint(CloudEndpoint.class);
				List<PlatformServiceCondition> conditions = endpoint.getPlatformServiceConditions(getId());
				
				CollectionComparator.compareCollection(serviceConditions, conditions, new CollectionComparator.Comparator<ServiceCondition, PlatformServiceCondition>() {
					@Override
					public boolean match(ServiceCondition o1, PlatformServiceCondition o2) {
						return o1.getId().equals(o2.getId());
					}
					@Override
					public void matched(ServiceCondition o1, PlatformServiceCondition o2) {
						o1.update(o2);
					}
					@Override
					public void afterO1(ServiceCondition o1) {
						internalRemoveProperty(p.serviceConditions, o1, serviceConditions);
					}
					@Override
					public void afterO2(PlatformServiceCondition o2) {
						internalAddProperty(p.serviceConditions, ServiceCondition.convert(o2), serviceConditions);
					}
				});
			} catch (CloudManagerException | InvalidRole_Exception | InvalidUserPass_Exception e) {
				throw new CloudModelException(e);
			}
		}
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (adapter == ICloudScope.class) {
			return this;
		} else if (adapter == IHinemosManager.class) {
			return getCloudScopes().getHinemosManager();
		}
		return super.getAdapter(adapter);
	}
	
	@Override
	public ExtendedProperty[] getExtendedProperties() {
		return extendedProperties.toArray(new ExtendedProperty[extendedProperties.size()]);
	}
	@Override
	public String getExtendedProperty(String name) {
		for (ExtendedProperty property: extendedProperties) {
			if (property.getName().equals(name))
				return property.getValue();
		}
		return null;
	}
	
	protected void updateExtendedProperties(List<com.clustercontrol.ws.xcloud.ExtendedProperty> extendedProperties) {
		CollectionComparator.compareCollection(this.extendedProperties, extendedProperties, new CollectionComparator.Comparator<ExtendedProperty, com.clustercontrol.ws.xcloud.ExtendedProperty>(){
			@Override
			public boolean match(ExtendedProperty o1, com.clustercontrol.ws.xcloud.ExtendedProperty o2) {
				return o1.getName().equals(o2.getName());
			}
			@Override
			public void matched(ExtendedProperty o1, com.clustercontrol.ws.xcloud.ExtendedProperty o2) {
				o1.setValue(o2.getValue());
			}
			@Override
			public void afterO1(ExtendedProperty o1) {
				internalRemoveProperty(p.extendedProperties, o1, CloudScope.this.extendedProperties);
			}
			@Override
			public void afterO2(com.clustercontrol.ws.xcloud.ExtendedProperty o2) {
				internalAddProperty(p.extendedProperties, ExtendedProperty.convert(o2), CloudScope.this.extendedProperties);
			}
		});
	}

	@Override
	public String getNodeId() {
		return nodeId;
	}
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	@Override
	public String getOwnerRoleId() {
		return ownerRoleId;
	}
	public void setOwnerRoleId(String ownerRoleId) {
		this.ownerRoleId = ownerRoleId;
	}
}
