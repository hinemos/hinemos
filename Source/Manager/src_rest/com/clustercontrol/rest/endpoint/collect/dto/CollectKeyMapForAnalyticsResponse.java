/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.collect.dto;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.clustercontrol.rest.annotation.msgconverter.RestPartiallyTransrateTarget;

public class CollectKeyMapForAnalyticsResponse {

	public CollectKeyMapForAnalyticsResponse(){
	}

	@RestPartiallyTransrateTarget
	private Map<String, CollectKeyInfoResponseP1> collectKeyMapForAnalytics = new ConcurrentHashMap<>();

	public Map<String, CollectKeyInfoResponseP1> getCollectKeyMapForAnalytics(){
		return this.collectKeyMapForAnalytics;
	}

	public void setCollectKeyMapForAnalytics(Map<String, CollectKeyInfoResponseP1> collectKeyMapForAnalytics){
		this.collectKeyMapForAnalytics = collectKeyMapForAnalytics;
	}

}
