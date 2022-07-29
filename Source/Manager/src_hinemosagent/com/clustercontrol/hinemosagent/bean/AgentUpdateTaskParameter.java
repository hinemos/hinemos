/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hinemosagent.bean;

import java.io.Serializable;

/**
 * Hinemosエージェントへのアップデート指示を行うタスクに渡すパラメータです。
 * 
 * @since 6.2.0
 */
public class AgentUpdateTaskParameter implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String facilityId;
	
	public AgentUpdateTaskParameter(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getFacilityId() {
		return facilityId;
	}

}
