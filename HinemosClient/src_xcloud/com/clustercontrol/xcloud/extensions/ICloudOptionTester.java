/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.extensions;

import java.util.Optional;

public interface ICloudOptionTester {
	Optional<Boolean> pluginTest(Object receiver, String property, Object[] args, Object expectedValue);
}
