/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.bean;

import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.xcloud.CloudManagerException;


public class ModifyPublicCloudScopeRequest extends ModifyCloudScopeRequest {
	@Override
	public void visit(IVisitor visitor) throws CloudManagerException, InvalidRole {
		visitor.visit(this);
	}
	@Override
	public <T> T transform(ITransformer<T> transformer) throws CloudManagerException, InvalidRole {
		return transformer.transform(this);
	}
}
