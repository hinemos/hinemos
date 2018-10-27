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

import com.clustercontrol.xcloud.model.cloud.IStorageBackupEntry;



public class StorageBackupEntryTester extends CloudOptionPropertyTester {
	@Override
	protected Optional<Boolean> resourceTest(Object receiver, String property, Object[] args, Object expectedValue) {
		switch(property) {
		case "status":
			String status = ((IStorageBackupEntry)receiver).getStatus();
			if (status == null) {
				return Optional.of(expectedValue == null);
			} else {
				return Optional.of(expectedValue != null ? Pattern.matches((String)expectedValue, status): false);
			}
		}
		return Optional.empty();
	}
}
