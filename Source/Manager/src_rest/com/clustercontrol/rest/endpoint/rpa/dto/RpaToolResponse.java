/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.rpa.dto;

public class RpaToolResponse {
	/** RPAツールID */
	private String rpaToolId;	
	/** RPAツール名 */
	private String rpaToolName;
	public RpaToolResponse() {
	}


	/** RPAツールID */
	public String getRpaToolId() {
		return this.rpaToolId;
	}

	public void setRpaToolId(String rpaToolId) {
		this.rpaToolId = rpaToolId;
	}

	/** RPAツール名 */
	public String getRpaToolName() {
		return this.rpaToolName;
	}

	public void setRpaToolName(String rpaToolName) {
		this.rpaToolName = rpaToolName;
	}
}
