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

import org.openapitools.client.model.InstanceInfoResponse;
import org.openapitools.client.model.NetworkInfoResponse;
import org.openapitools.client.model.StorageInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.model.CloudModelException;
import com.clustercontrol.xcloud.model.base.Element;
import com.clustercontrol.xcloud.util.CloudRestClientWrapper;
import com.clustercontrol.xcloud.util.CollectionComparator;

public class ComputeResources extends Element implements IComputeResources {
	private List<Instance> instances = new ArrayList<>();
	private List<Storage> storages = new ArrayList<>();
	private List<Network> networks = new ArrayList<>();
	
	public ComputeResources(Location location) {
		setOwner(location);
	}
	
	@Override
	public Location getLocation() {
		return (Location)getOwner();
	}

	@Override
	public Instance[] getInstances() {
		return instances.toArray(new Instance[instances.size()]);
	}

	@Override
	public Instance getInstance(String instanceId) {
		for (Instance instance: instances) {
			if (instance.getId().equals(instanceId))
				return instance;
		}
		throw new CloudModelException(String.format("Not found instance of %s", instanceId));
	}
	
	public void updateInstances(List<InstanceInfoResponse> webInstances) {
		CollectionComparator.compareCollection(instances, webInstances, new CollectionComparator.Comparator<Instance, InstanceInfoResponse>() {
			public boolean match(Instance o1, InstanceInfoResponse o2) {return o1.equalValues(o2);}
			public void matched(Instance o1, InstanceInfoResponse o2) {o1.update(o2);}
			public void afterO1(Instance o1) {internalRemoveProperty(p.instances, o1, instances);}
			public void afterO2(InstanceInfoResponse o2) {
				Instance newInstance = Instance.convert(o2);
				internalAddProperty(p.instances, newInstance, instances);
			}
		});
	}
	
	@Override
	public Storage[] getStorages() {
		if (storages == null)
			return new Storage[]{};
		return storages.toArray(new Storage[storages.size()]);
	}
	
	@Override
	public void updateStorages() {
		try {
			CloudRestClientWrapper wrapper = getLocation().getCloudScope().getCloudScopes().getHinemosManager().getWrapper();
			List <StorageInfoResponse> responses = wrapper.getStorages(getLocation().getCloudScope().getId(), getLocation().getId(), null);
			updateStorages(responses);
		} catch (CloudManagerException | InvalidUserPass | InvalidRole | RestConnectFailed | HinemosUnknown e) {
			throw new CloudModelException(e);
		}
	}	
	
	public void updateStorages(List <StorageInfoResponse> webStorages) {
		if (storages == null)
			storages = new ArrayList<>();
		
		CollectionComparator.compareCollection(storages, webStorages, new CollectionComparator.Comparator<Storage, StorageInfoResponse>() {
			public boolean match(Storage o1, StorageInfoResponse o2) {return o1.equalValues(o2);}
			public void matched(Storage o1, StorageInfoResponse o2) {o1.update(o2);}
			public void afterO1(Storage o1) {internalRemoveProperty(p.storages, o1, storages);}
			public void afterO2(StorageInfoResponse o2) {
				Storage newStorage = Storage.convert(o2);
				internalAddProperty(p.storages, newStorage, storages);
			}
		});
	}	

	@Override
	public Network[] getNetworks() {
		if (networks == null)
			return new Network[]{};
		return networks.toArray(new Network[networks.size()]);
	}

	@Override
	public Network[] getNetworksWithInitializing() {
		if (networks.isEmpty())
			updateNetworks();
		return networks.toArray(new Network[networks.size()]);
	}
	
	@Override
	public void updateNetworks() {
		if (networks == null)
			networks = new ArrayList<>();
		
		CloudRestClientWrapper wrapper = getLocation().getCloudScope().getCloudScopes().getHinemosManager().getWrapper();
		try {
			List <NetworkInfoResponse> webStorages = wrapper.getAllNetworks(getLocation().getCloudScope().getId(), getLocation().getId());
			CollectionComparator.compareCollection(networks, webStorages, new CollectionComparator.Comparator<Network, NetworkInfoResponse>() {
				public boolean match(Network o1, NetworkInfoResponse o2) {return o1.equalValues(o2);}
				public void matched(Network o1, NetworkInfoResponse o2) {o1.update(o2);}
				public void afterO1(Network o1) {internalRemoveProperty(p.networks, o1, networks);}
				public void afterO2(NetworkInfoResponse o2) {
					Network newStorage = Network.convert(o2);
					internalAddProperty(p.networks, newStorage, networks);
				}
			});
		} catch (CloudManagerException | InvalidUserPass | InvalidRole | RestConnectFailed | HinemosUnknown e) {
			throw new CloudModelException(e);
		}
	}
}
