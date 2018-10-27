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

import com.clustercontrol.xcloud.model.cloud.IStorage;

public class StorageTester extends CloudOptionPropertyTester {
	@Override
	protected Optional<Boolean> resourceTest(Object receiver, String property, Object[] args, Object expectedValue) {
		IStorage storage = (IStorage)receiver;
		switch(property) {
		case "targetInstanceId":
			String targetInstanceId = storage.getTargetInstanceId();
			if (targetInstanceId == null) {
				return Optional.of(expectedValue == null);
			} else {
				return Optional.of(expectedValue != null ? Pattern.matches((String)expectedValue, targetInstanceId): false);
			}
		}
		return Optional.empty();
	}
}
