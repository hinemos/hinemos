/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.plugin.tester;

import org.eclipse.core.expressions.PropertyTester;

import com.clustercontrol.xcloud.model.cloud.ICloudScope;
import com.clustercontrol.xcloud.model.cloud.ILocation;
import com.clustercontrol.xcloud.model.cloud.IServiceCondition;

public class ServiceConditionTester extends PropertyTester {
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		IServiceCondition cloudService = (IServiceCondition)receiver;
		switch(property) {
		case "handler":
			{
				ICloudScope cloudScope;
				if (cloudService.getOwner() instanceof ILocation) {
					ILocation location = (ILocation) cloudService.getOwner();
					cloudScope = location.getCloudScope();
				} else if (cloudService.getOwner() instanceof ICloudScope) {
					cloudScope = (ICloudScope) cloudService.getOwner();
				} else {
					return false;
				}
				return new CloudScopeTester().test(cloudScope, property, args, expectedValue);
			}
		}
		return false;
	}
}
