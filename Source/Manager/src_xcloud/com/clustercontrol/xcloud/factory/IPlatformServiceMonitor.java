/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

import java.util.List;

import com.clustercontrol.xcloud.CloudManagerException;
import com.clustercontrol.xcloud.InternalManagerError;
import com.clustercontrol.xcloud.factory.ICloudOption.PlatformServiceCondition;
import com.clustercontrol.xcloud.model.CloudScopeEntity;
import com.clustercontrol.xcloud.model.LocationEntity;

public interface IPlatformServiceMonitor {
	public interface IPlatformAreaMonitor extends IPlatformServiceMonitor {
		List<PlatformServiceCondition> getPlatformServiceConditions() throws CloudManagerException;
		List<PlatformServiceCondition> getPlatformServiceConditions(LocationEntity location) throws CloudManagerException;
		List<PlatformServiceCondition> monitorPlatformServiceConditions() throws CloudManagerException;
		List<PlatformServiceCondition> monitorPlatformServiceConditions(LocationEntity location) throws CloudManagerException;
	}
	
	public interface ICloudScopeAreaMonitor extends IPlatformServiceMonitor {
		List<PlatformServiceCondition> getPlatformServiceConditions(CloudScopeEntity scope) throws CloudManagerException;
		List<PlatformServiceCondition> getPlatformServiceConditions(CloudScopeEntity scope, LocationEntity location) throws CloudManagerException;
		List<PlatformServiceCondition> monitorPlatformServiceConditions(CloudScopeEntity scope) throws CloudManagerException;
		List<PlatformServiceCondition> monitorPlatformServiceConditions(CloudScopeEntity scope, LocationEntity location) throws CloudManagerException;
	}
	
	interface ITransformer<T> {
		T transform(IPlatformAreaMonitor monitor) throws CloudManagerException;
		T transform(ICloudScopeAreaMonitor monitor) throws CloudManagerException;
	}

	interface IVisitor {
		void visit(IPlatformAreaMonitor monitor) throws CloudManagerException;
		void visit(ICloudScopeAreaMonitor monitor) throws CloudManagerException;
	}
	
	public static class Visitor implements IVisitor {
		@Override
		public void visit(IPlatformAreaMonitor monitor) throws CloudManagerException {
			throw new InternalManagerError();
		}
		@Override
		public void visit(ICloudScopeAreaMonitor monitor) throws CloudManagerException {
			throw new InternalManagerError();
		}
	}
	
	String getPlatformId();
	void visit(IVisitor visitor) throws CloudManagerException;
	<T> T transform(ITransformer<T> transformer) throws CloudManagerException;
}
