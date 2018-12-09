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

import javax.persistence.TypedQuery;

import com.clustercontrol.commons.util.HinemosEntityManager;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.Session;
import com.clustercontrol.xcloud.bean.ExtendedProperty;
import com.clustercontrol.xcloud.bean.Network;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.CloudScopeEntity.OptionCallable;
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
						TypedQuery<InstanceEntity> query =  em.createNamedQuery(InstanceEntity.findInstancesByInstanceIds, InstanceEntity.class);
						query.setParameter("cloudScopeId", getCloudScope().getId());
						query.setParameter("locationId", getLocation().getLocationId());
						query.setParameter("instanceIds", platformNetwork.getAttachedInstanceIds());

						for (InstanceEntity entity: query.getResultList()) {
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
