/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.plugin.tester;

import org.eclipse.core.expressions.PropertyTester;

import com.clustercontrol.xcloud.model.cloud.ILocation;

public class LocationTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		ILocation location = (ILocation)receiver;
		switch(property) {
		case "handler":
			return new CloudScopeTester().test(location.getCloudScope(), property, args, expectedValue);
		}
		return false;
	}
}
