/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.plugin.tester;

import java.util.Optional;
import java.util.regex.Pattern;

import com.clustercontrol.xcloud.model.cloud.INetwork;



public class NetworkTester extends CloudOptionPropertyTester {
	@Override
	protected Optional<Boolean> resourceTest(Object receiver, String property, Object[] args, Object expectedValue) {
		INetwork network = (INetwork)receiver;
		switch(property) {
		case "networkType":
			String networkType = network.getNetworkType();
			if (networkType == null) {
				return Optional.of(expectedValue == null);
			} else {
				return Optional.of(expectedValue != null ? Pattern.matches((String)expectedValue, networkType): false);
			}
		}
		return Optional.empty();
	}
}
