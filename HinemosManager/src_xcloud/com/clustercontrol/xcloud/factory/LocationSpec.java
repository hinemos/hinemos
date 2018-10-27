/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.xcloud.factory;

public class LocationSpec {
	private String locationType;
	
	public LocationSpec(String locationType) {
		this.locationType = locationType;
	}
	
	public String getLocationType() {
		return locationType;
	}
}
