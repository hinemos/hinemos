/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.snmptrap.bean;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlType;

/**
 * 受信した SNMPTRAP 情報を内部形式にて保持する<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
@XmlType(namespace = "http://monitor.ws.clustercontrol.com")
public class SnmpTrap implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private TrapId trapId;

	private String community;
	private long receivedTime;

	private String agentAddr;
	private long sysUptime;

	private List<SnmpVarBind> varBinds = Collections.emptyList();
	
	// for cluster jax-ws
	public SnmpTrap() { }
	
	public SnmpTrap(
		TrapId oid,
		long receivedTime,
		String community,
		String agentAddr,
		long sysUptime,
		List<SnmpVarBind> varbinds
		) {
		this.trapId = oid;
		this.community = community;
		this.receivedTime = receivedTime;
		this.agentAddr = agentAddr;
		this.sysUptime = sysUptime;
		this.varBinds = varbinds;
	}
	
	public void setTrapId(TrapId trapId) {
		this.trapId = trapId;
	}
	
	public TrapId getTrapId() {
		return trapId;
	}
	
	public void setCommunity(String community) {
		this.community = community;
	}
	
	public String getCommunity() {
		return community;
	}
	
	public void setReceivedTime(long receivedTime) {
		this.receivedTime = receivedTime;
	}
	
	public long getReceivedTime() {
		return receivedTime;
	}
	
	public void setAgentAddr(String agentAddr) {
		this.agentAddr = agentAddr;
	}
	
	public String getAgentAddr() {
		return agentAddr;
	}
	
	public void setSysUptime(long sysUptime) {
		this.sysUptime = sysUptime;
	}
	
	public long getSysUptime() {
		return sysUptime;
	}

	public void setVarBinds(List<SnmpVarBind> varBinds) {
		this.varBinds = varBinds;
	}
	
	public List<SnmpVarBind> getVarBinds() {
		return varBinds;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((agentAddr == null) ? 0 : agentAddr.hashCode());
		result = prime * result
				+ ((community == null) ? 0 : community.hashCode());
		result = prime * result + (int) (receivedTime ^ (receivedTime >>> 32));
		result = prime * result + (int) (sysUptime ^ (sysUptime >>> 32));
		result = prime * result + ((trapId == null) ? 0 : trapId.hashCode());
		result = prime * result
				+ ((varBinds == null) ? 0 : varBinds.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SnmpTrap other = (SnmpTrap) obj;
		if (agentAddr == null) {
			if (other.agentAddr != null)
				return false;
		} else if (!agentAddr.equals(other.agentAddr))
			return false;
		if (community == null) {
			if (other.community != null)
				return false;
		} else if (!community.equals(other.community))
			return false;
		if (receivedTime != other.receivedTime)
			return false;
		if (sysUptime != other.sysUptime)
			return false;
		if (trapId == null) {
			if (other.trapId != null)
				return false;
		} else if (!trapId.equals(other.trapId))
			return false;
		if (varBinds == null) {
			if (other.varBinds != null)
				return false;
		} else if (!varBinds.equals(other.varBinds))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SnmpTrap [trapId=" + trapId + ", community=" + community
				+ ", receivedTime=" + receivedTime + ", agentAddr=" + agentAddr
				+ ", sysUptime=" + sysUptime + ", varbinds=" + varBinds + "]";
	}
}
