/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.rpa.dto;

public class RpaToolEnvResponse {
	/** 環境毎のRPAツールID */
	private String rpaToolEnvId;	
	/** 環境毎のRPAツール名 */
	private String rpaToolEnvName;
	public RpaToolEnvResponse() {
	}


	/** 環境毎のRPAツールID */
	public String getRpaToolEnvId() {
		return rpaToolEnvId;
	}


	public void setRpaToolEnvId(String rpaToolEnvId) {
		this.rpaToolEnvId = rpaToolEnvId;
	}


	/** 環境毎のRPAツール名 */
	public String getRpaToolEnvName() {
		return rpaToolEnvName;
	}


	public void setRpaToolEnvName(String rpaToolEnvName) {
		this.rpaToolEnvName = rpaToolEnvName;
	}
}
