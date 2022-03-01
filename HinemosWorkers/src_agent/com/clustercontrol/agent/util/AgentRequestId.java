/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.util;

import java.util.UUID;

/**
 * リクエストに付与するユニークIDです。
 */
public class AgentRequestId {

	private final String idValue;

	public AgentRequestId() {
		idValue = UUID.randomUUID().toString();
	}

	public String toRequestHeaderValue() {
		return idValue;
	}

	@Override
	public String toString() {
		return idValue;
	}

}
