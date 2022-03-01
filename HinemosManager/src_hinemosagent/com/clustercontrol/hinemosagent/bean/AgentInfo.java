/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.hinemosagent.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.agent.bean.DhcpUpdateMode;
import com.clustercontrol.commons.util.HinemosPropertyCommon;
import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.repository.util.DhcpSupport;
import com.clustercontrol.util.HinemosTime;

@XmlType(namespace = "http://agent.ws.clustercontrol.com")
public class AgentInfo implements Cloneable, Serializable {
	private static final Log m_log = LogFactory.getLog(AgentInfo.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String facilityId = "";
	private String hostname = "";
	private ArrayList<String> ipAddressList = new ArrayList<String>();
	private int interval = 0;
	private String instanceId = "";

	// firstLoginはエージェントの起動時刻なので、エージェント側で入力
	// HinemosTime.getDateInstance().getTime()を利用する。
	private long startupTime;
	// lastLoginはマネージャ側で入力
	private long lastLogin;
	/**
	 * DHCPサポート機能におけるノード更新方法の定義
	 * エージェントプロパティ"dhcp.update.node"で指定
	 * 既定値(v.6.2以前含)はdisable
	 */
	private DhcpUpdateMode dhcpUpdateMode = DhcpUpdateMode.disable;

	/**
	 * スコープ自動登録機能の対象スコープ
	 * エージェントプロパティ"repository.autoassign.scopeids"で指定
	 */
	private List<String> assignScopeList = new ArrayList<>();

	// Hinemosエージェントのバージョン
	// エージェントから連携される（対応していない7.0以前の過去バージョンは空になる）
	private String version = "";

	public void refreshLastLogin() {
		lastLogin = HinemosTime.currentTimeMillis();
	}
	public boolean isValid() {
		/*
		 * (interval * monitor.agent.valid.multi + monitor.agent.valid.plus) の時間でgetTopicがない場合は、無効とみなす。
		 */
		int intervalMulti = HinemosPropertyCommon.monitor_agent_valid_multi.getIntegerValue();
		int intervalPlus = HinemosPropertyCommon.monitor_agent_valid_plus.getIntegerValue(); // 10sec
		if (interval * intervalMulti + intervalPlus > HinemosTime.currentTimeMillis() - lastLogin) {
			return true;
		}
		
		if (getDhcpUpdateMode().equals(DhcpUpdateMode.ip)) {
			try {
				// DHCPサポート機能が有効な場合ノードを無効化する。
				DhcpSupport.nullfyNodes(facilityId);
			} catch (FacilityNotFound e) {
				// ノードが削除されている場合、何もしない
			} catch (HinemosUnknown e) {
				// 通常来ない想定
				m_log.warn(e.getMessage(), e);
			}
		}

		return false;
	}

	/*
	 * getter/setter
	 */
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
	public ArrayList<String> getIpAddress() {
		return ipAddressList;
	}
	public void setIpAddress(ArrayList<String> ipAddressList) {
		this.ipAddressList = ipAddressList;
	}
	public int getInterval() {
		return interval;
	}
	public void setInterval(int interval) {
		this.interval = interval;
	}
	public long getStartupTime() {
		return startupTime;
	}
	public void setStartupTime(long startupTime) {
		this.startupTime = startupTime;
	}
	public long getLastLogin() {
		return lastLogin;
	}
	public String getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
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

	@XmlTransient
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder(new Date(startupTime) + "," + new Date(lastLogin) + ",(" + interval + ")");
		str.append("[");
		if (facilityId != null) {
			str.append(facilityId);
		}
		if (instanceId != null) {
			str.append("," + instanceId);
		}
		str.append(",");
		if (hostname != null) {
			str.append(hostname);
		}
		str.append("]");
		for (String ipAddress : ipAddressList) {
			str.append(ipAddress + ",");
		}
		return str.toString();
	}

	@Override
	public AgentInfo clone() {
		try {
			AgentInfo clone = (AgentInfo) super.clone();
			return clone;
		} catch (CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
	}
}
