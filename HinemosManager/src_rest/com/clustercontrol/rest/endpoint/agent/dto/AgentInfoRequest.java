/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rest.endpoint.agent.dto;

import java.util.ArrayList;
import java.util.List;

import com.clustercontrol.agent.bean.DhcpUpdateMode;
import com.clustercontrol.hinemosagent.bean.AgentInfo;
import com.clustercontrol.rest.annotation.beanconverter.RestBeanConvertAssertion;

@RestBeanConvertAssertion(to = AgentInfo.class)
public class AgentInfoRequest extends AgentRequestDto {

	// ---- from AgentInfo
	private String facilityId;
	private String hostname;
	private ArrayList<String> ipAddressList;
	private Integer interval;
	private String instanceId;
	private Long startupTime;
	private Long lastLogin;
	/**
	 * DHCPサポート機能におけるノード更新方法の定義
	 * エージェントプロパティ"dhcp.update.node"で指定
	 */
	private DhcpUpdateMode dhcpUpdateMode;
	/**
	 * スコープ自動登録機能の対象スコープ
	 * エージェントプロパティ"repository.autoassign.scopeids"で指定
	 */
	public List<String> assignScopeList = new ArrayList<>();
	private String version;

	public AgentInfoRequest() {
	}

	public String getFacilityId() {
		return facilityId;
	}

	public void setFacilityId(String facilityId) {
		this.facilityId = facilityId;
	}

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public ArrayList<String> getIpAddressList() {
		return ipAddressList;
	}

	public void setIpAddressList(ArrayList<String> ipAddressList) {
		this.ipAddressList = ipAddressList;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

	public String getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}

	public Long getStartupTime() {
		return startupTime;
	}

	public void setStartupTime(Long startupTime) {
		this.startupTime = startupTime;
	}

	public Long getLastLogin() {
		return lastLogin;
	}

	public void setLastLogin(Long lastLogin) {
		this.lastLogin = lastLogin;
	}

	public void setInterval(Integer interval) {
		this.interval = interval;
	}

	/**
	 * DHCPサポート機能におけるノード更新方法の定義
	 * エージェントプロパティ"dhcp.update.node"で指定
	 */
	public DhcpUpdateMode getDhcpUpdateMode() {
		return dhcpUpdateMode;
	}

	public void setDhcpUpdateMode(DhcpUpdateMode dhcpUpdateMode) {
		this.dhcpUpdateMode = dhcpUpdateMode;
	}

	/**
	 * スコープ自動登録機能の対象スコープ
	 * エージェントプロパティ"repository.autoassign.scopeids"で指定
	 */
	public List<String> getAssignScopeList() {
		return assignScopeList;
	}

	public void setAssignScopeList(List<String> assignScopeList) {
		this.assignScopeList = assignScopeList;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
