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
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.base.Element;
import com.clustercontrol.xcloud.model.repository.IScope;
import com.clustercontrol.xcloud.util.CollectionComparator;

public class Location extends Element implements ILocation {
	private ComputeResources computeResources;
	
	private List<ServiceCondition> serviceConditions;
	
	private CloudScope cloudScope;
	private String id;
	private String name;
	private String locationType;
	private String entryType;
	
	private List<Endpoint> endpoints = new ArrayList<>();

	private IScope counterSope;
	
	public static Location convert(CloudScope cloudScope, com.clustercontrol.ws.xcloud.Location source) {
		Location location = new Location(cloudScope);
		location.update(source);
		return location;
	}
	
	public Location(CloudScope cloudScope) {
		this.cloudScope = cloudScope;
		this.computeResources = new ComputeResources(this);
	}

	@Override
	public CloudScope getCloudScope() {return cloudScope;}

	@Override
	public String getId() {return id;}
	public void setId(String id) {this.id = id;}
	
	@Override
	public String getName() {return name;}
	public void setName(String name) {this.name = name;}

	@Override
	public String getEntryType() {return entryType;}
	public void setEntryType(String entryType) {this.entryType = entryType;}

	@Override
	public String getLocationType() {return locationType;}
	public void setLocationType(String locationType) {this.locationType = locationType;}
	
	public CloudEndpoint getEndpoint() {
		return getCloudScope().getEndpoint();
	}
	
	public void update(com.clustercontrol.ws.xcloud.Location location) {
		setId(location.getId());
		setName(location.getName());
		setEntryType(location.getEntryType().name());
		setLocationType(location.getLocationType());
		
		CollectionComparator.compareCollection(endpoints, location.getEndpoints(), new CollectionComparator.Comparator<Endpoint, com.clustercontrol.ws.xcloud.Endpoint>() {
			@Override
			public boolean match(Endpoint o1, com.clustercontrol.ws.xcloud.Endpoint o2) {
				return o1.getId().equals(o2.getId());
			}
			@Override
			public void matched(Endpoint o1, com.clustercontrol.ws.xcloud.Endpoint o2) {
				o1.update(Location.this, o2);
			}
			@Override
			public void afterO1(Endpoint o1) {
				internalRemoveProperty(p.endpoints, o1, endpoints);
			}
			@Override
			public void afterO2(com.clustercontrol.ws.xcloud.Endpoint o2) {
				internalAddProperty(p.endpoints, Endpoint.convert(Location.this, o2), endpoints);
			}
		});
	}
	
	public boolean equalValues(com.clustercontrol.ws.xcloud.Location source) {
		assert source != null;
		return this.getId().equals(source.getId());
	}

	@Override
	public ComputeResources getComputeResources() {
		return computeResources;
	}

	@Override
	public IScope getCounterScope() {
		return counterSope;
	}
	public void setCounterScope(IScope counterSope) {
		this.counterSope = counterSope;
	}

	@Override
	public void updateLocation() {
		getCloudScope().getCloudScopes().getHinemosManager().updateLocation(this);
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
				CloudEndpoint endpoint = getCloudScope().getCloudScopes().getHinemosManager().getEndpoint(CloudEndpoint.class);
				
				List<PlatformServiceCondition> conditions = endpoint.getPlatformServiceConditionsByLocation(getCloudScope().getId(), getId());
				for (PlatformServiceCondition condition: conditions) {
					internalAddProperty(p.serviceConditions, ServiceCondition.convert(condition), serviceConditions);
				}
			} catch (CloudManagerException | InvalidRole_Exception | InvalidUserPass_Exception e) {
				throw new CloudModelException(e.getMessage(), e);
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
				CloudEndpoint endpoint = getCloudScope().getCloudScopes().getHinemosManager().getEndpoint(CloudEndpoint.class);
				List<PlatformServiceCondition> conditions = endpoint.getPlatformServiceConditionsByLocation(getCloudScope().getId(), getId());
				
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
				throw new CloudModelException(e.getMessage(), e);
			}
		}
	}
	

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter) {
		if (adapter == ICloudScope.class) {
			return getCloudScope();
		} else if (adapter == ILocation.class) {
			return this;
		} else if (adapter == IHinemosManager.class) {
			if (getCloudScope() != null && getCloudScope().getCloudScopes() != null)
				return getCloudScope().getCloudScopes().getHinemosManager();
		}
		return super.getAdapter(adapter);
	}

	@Override
	public String toString() {
		return "Location [id=" + id + ", name=" + name + ", locationType="
				+ locationType + ", entryType=" + entryType + "]";
	}

	@Override
	public Endpoint[] getEndpoints() {
		return endpoints.toArray(new Endpoint[endpoints.size()]);
	}
}
