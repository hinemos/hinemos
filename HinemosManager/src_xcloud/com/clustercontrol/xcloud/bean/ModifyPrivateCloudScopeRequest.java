/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import java.util.List;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.validation.annotation.ElementId;
import com.clustercontrol.xcloud.validation.annotation.Into;
import com.clustercontrol.xcloud.validation.annotation.NotNull;


public class ModifyPrivateCloudScopeRequest extends ModifyCloudScopeRequest {
	private List<PrivateLocation> privateLocations;
	
	@ElementId("privateLocations")
	@NotNull
	@Into
	public List<PrivateLocation> getPrivateLocations() {
		return privateLocations;
	}
	public void setPrivateLocations(List<PrivateLocation> privateLocations) {
		this.privateLocations = privateLocations;
	}
	@Override
	public void visit(IVisitor visitor) throws CloudManagerException, InvalidRole {
		visitor.visit(this);
	}
	@Override
	public <T> T transform(ITransformer<T> transformer) throws CloudManagerException, InvalidRole {
		return transformer.transform(this);
	}
}
