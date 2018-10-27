/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.model;

import java.util.List;
import java.util.Map;

public interface IAssignableEntity {
	String getName();
	
	String getFacilityId();
	
	List<String> getIpAddresses();
	
	Map<String, ResourceTag> getTags();
	
	default boolean isPatternEnabled() {
		return true;
	}
	
	default boolean isTagEnabled() {
		return true;
	}
}
