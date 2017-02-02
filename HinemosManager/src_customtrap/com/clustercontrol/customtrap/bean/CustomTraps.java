/*

 Copyright (C) 2016 NTT DATA Corporation

 This program is free software; you can redistribute it and/or
 Modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation, version 2.

 This program is distributed in the hope that it will be
 useful, but WITHOUT ANY WARRANTY; without even the implied
 warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 PURPOSE.  See the GNU General Public License for more details.

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
