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

import org.openapitools.client.model.EndpointEntityResponse;
import org.openapitools.client.model.LocationInfoResponse;
import org.openapitools.client.model.PlatformServiceConditionResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.base.Element;
import com.clustercontrol.xcloud.model.repository.IScope;
import com.clustercontrol.xcloud.util.CloudRestClientWrapper;
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
	
	public static Location convert(CloudScope cloudScope, LocationInfoResponse source) {
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
	
	public CloudRestClientWrapper getWrapper() {
		return getCloudScope().getWrapper();
	}
	
	public void update(LocationInfoResponse location) {
		setId(location.getId());
		setName(location.getName());
		setEntryType(location.getEntryType().name());
		setLocationType(location.getLocationType());
		
		CollectionComparator.compareCollection(endpoints, location.getEndpoints(), new CollectionComparator.Comparator<Endpoint, EndpointEntityResponse>() {
			@Override
			public boolean match(Endpoint o1, EndpointEntityResponse o2) {
				return o1.getId().equals(o2.getEndpointId());
			}
			@Override
			public void matched(Endpoint o1, EndpointEntityResponse o2) {
				o1.update(Location.this, o2);
			}
			@Override
			public void afterO1(Endpoint o1) {
				internalRemoveProperty(p.endpoints, o1, endpoints);
			}
			@Override
			public void afterO2(EndpointEntityResponse o2) {
				internalAddProperty(p.endpoints, Endpoint.convert(Location.this, o2), endpoints);
			}
		});
	}
	
	public boolean equalValues(LocationInfoResponse source) {
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
				CloudRestClientWrapper wrapper = getCloudScope().getCloudScopes().getHinemosManager().getWrapper();
				
				List<PlatformServiceConditionResponse> conditions = wrapper.getPlatformServiceConditions(getCloudScope().getId(), getId(), null);
				for (PlatformServiceConditionResponse condition: conditions) {
					internalAddProperty(p.serviceConditions, ServiceCondition.convert(condition), serviceConditions);
				}
			} catch (CloudManagerException | InvalidUserPass | InvalidRole | RestConnectFailed | HinemosUnknown  e) {
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
				CloudRestClientWrapper endpoint = getCloudScope().getCloudScopes().getHinemosManager().getWrapper();
				List<PlatformServiceConditionResponse> conditions = endpoint.getPlatformServiceConditions(getCloudScope().getId(), getId(), null);
				
				CollectionComparator.compareCollection(serviceConditions, conditions, new CollectionComparator.Comparator<ServiceCondition, PlatformServiceConditionResponse>() {
					@Override
					public boolean match(ServiceCondition o1, PlatformServiceConditionResponse o2) {
						return o1.getId().equals(o2.getId());
					}
					@Override
					public void matched(ServiceCondition o1, PlatformServiceConditionResponse o2) {
						o1.update(o2);
					}
					@Override
					public void afterO1(ServiceCondition o1) {
						internalRemoveProperty(p.serviceConditions, o1, serviceConditions);
					}
					@Override
					public void afterO2(PlatformServiceConditionResponse o2) {
						internalAddProperty(p.serviceConditions, ServiceCondition.convert(o2), serviceConditions);
					}
				});
			} catch (CloudManagerException | InvalidUserPass | InvalidRole | RestConnectFailed | HinemosUnknown e) {
				// 更新に失敗した場合は、過去の情報がクラウド[サービス状態]に表示されるのを防ぐため、
				// 過去の情報を削除しておく
				List<ServiceCondition> tmpCloneListForDelete = new ArrayList<>(serviceConditions);
				for (ServiceCondition deleteCond : tmpCloneListForDelete) {
					internalRemoveProperty(p.serviceConditions, deleteCond, serviceConditions);
				}
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
