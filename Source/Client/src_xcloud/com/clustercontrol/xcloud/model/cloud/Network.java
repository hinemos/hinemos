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

import org.openapitools.client.model.NetworkInfoResponse;

public class Network extends Resource implements INetwork {
	protected String id = null;
	protected String name = null;
	protected String networkType = null;
	protected List<String> attachedInstances = new ArrayList<>();

	public Network() {
	}

	@Override
	public String getId() {return id;}
	public void setId(String id) {this.id = id;}

	@Override
	public String getName() {return name;}
	public void setName(String name) {internalSetProperty(INetwork.p.name, name, ()->this.name, (s)->this.name=s);}

	@Override
	public String getNetworkType() {return networkType;}
	public void setNetworkType(String networkType) {internalSetProperty(INetwork.p.networkType, networkType, ()->this.networkType, (s)->this.networkType=s);}

	@Override
	public ComputeResources getCloudCompute() {return (ComputeResources)getOwner();}

	public static Network convert(NetworkInfoResponse source) {
		Network network = new Network();
		network.update(source);
		return network;
	}
	
	public boolean equalValues(NetworkInfoResponse source) {
		return getId().equals(source.getId());
	}

	protected void update(NetworkInfoResponse source) {
		setId(source.getId());
		setName(source.getName());
		setNetworkType(source.getResourceTypeAsPlatform());
		
		getAttachedInstances().clear();
		getAttachedInstances().addAll(source.getAttachedInstances());
		
		updateExtendedProperties(source.getExtendedProperties());
	}

	@Override
	public Location getLocation() {
		return getCloudCompute().getLocation();
	}

	@Override
	public List<String> getAttachedInstances() {
		return attachedInstances;
	}
}
