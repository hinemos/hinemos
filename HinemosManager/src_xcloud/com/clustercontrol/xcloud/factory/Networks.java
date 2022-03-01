/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.TypedQuery;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.bean.ExtendedProperty;
import com.clustercontrol.xcloud.bean.Network;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity.OptionCallable;
import com.clustercontrol.xcloud.util.CloudUtil;
import com.clustercontrol.xcloud.model.InstanceEntity;

public class Networks extends Resources implements INetworks {
	
	@Override
	public List<Network> getAllNetwork() throws CloudManagerException {
		return getCloudScope().optionCall(new OptionCallable<List<Network>>() {
			@Override
			public List<Network> call(CloudScopeEntity scope, ICloudOption option) throws CloudManagerException {
				List<Network> networks = new ArrayList<>();
				List<IResourceManagement.Network> platformNetworks = option.getResourceManagement(getLocation(), getUser()).getNetworks();
				
				HinemosEntityManager em = Session.current().getEntityManager();
				
				for (IResourceManagement.Network platformNetwork: platformNetworks) {
					Network network = new Network();
					network.setId(platformNetwork.getResourceId());
					network.setName(platformNetwork.getName());
					network.setCloudScopeId(getCloudScope().getId());
					network.setLocationId(getLocation().getLocationId());
					network.setResourceTypeAsPlatform(platformNetwork.getResourceTypeAsPlatform());
					
					if (!platformNetwork.getAttachedInstanceIds().isEmpty()) {
						int start_idx = 0;
						int end_idx = 0;
						List<InstanceEntity> entities = new ArrayList<>();
						List<String> queryInstanceIds = platformNetwork.getAttachedInstanceIds();
						while(start_idx < queryInstanceIds.size()){
							TypedQuery<InstanceEntity> query =  em.createNamedQuery(InstanceEntity.findInstancesByInstanceIds, InstanceEntity.class);
							query.setParameter("cloudScopeId", getCloudScope().getId());
							query.setParameter("locationId", getLocation().getLocationId());
							end_idx = start_idx + CloudUtil.SQL_PARAM_NUMBER_THRESHOLD;
							if (end_idx > queryInstanceIds.size()){
								end_idx = queryInstanceIds.size();
							}

							List<String> subList = queryInstanceIds.subList(start_idx, end_idx);
							query.setParameter("instanceIds", subList);
							entities.addAll(query.getResultList());
							start_idx = end_idx;
						}

						for (InstanceEntity entity: entities) {
							network.getAttachedInstances().add(entity.getResourceId());
						}
					}
					
					for (IResourceManagement.ExtendedProperty entry: platformNetwork.getExtendedProperty()) {
						ExtendedProperty property = new ExtendedProperty();
						property.setName(entry.getName());
						property.setValue(entry.getValue());
						network.getExtendedProperties().add(property);
					}
					networks.add(network);
				}
				return networks;
			}
		});
	}
}
