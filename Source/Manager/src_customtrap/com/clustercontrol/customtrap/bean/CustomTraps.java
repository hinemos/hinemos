/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.customtrap.bean;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 受信した 全カスタムトラップ情報を内部形式にて保持する<BR>
 *
 * @version 6.0.0
 * @since 6.0.0
 */
public class CustomTraps implements Serializable {
	private static final long serialVersionUID = 1L;

	/** ファシリティID */
	@JsonProperty("FacilityID")
	private String facilityId;
	public String getFacilityId() {
		return facilityId;
	}
	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}
	
	/** カスタムトラップ情報配列 */
	@JsonProperty("DATA")
    private List<CustomTrap> customTraps;
    public List<CustomTrap> getCustomTraps() {
        return customTraps;
    }

    public void setCustomTraps(List<CustomTrap> customTraps) {
        this.customTraps = customTraps;
    }

    /* Agentのアドレス */
	@JsonIgnore
	private String agentAddr;

	public String getAgentAddr() {
		return agentAddr;
	}

	public void setAgentAddr(String agentAddr) {
		this.agentAddr = agentAddr;
	}    
    
}
