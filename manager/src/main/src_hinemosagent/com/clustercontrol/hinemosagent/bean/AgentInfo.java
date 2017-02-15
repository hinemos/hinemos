package com.clustercontrol.hinemosagent.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.bind.annotation.XmlType;

import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.util.HinemosTime;

@XmlType(namespace = "http://agent.ws.clustercontrol.com")
public class AgentInfo implements Cloneable, Serializable {
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

	public void refreshLastLogin() {
		lastLogin = HinemosTime.currentTimeMillis();
	}
	public boolean isValid() {
		/*
		 * (interval * monitor.agent.valid.multi + monitor.agent.valid.plus) の時間でgetTopicがない場合は、無効とみなす。
		 */
		int intervalMulti = HinemosPropertyUtil.getHinemosPropertyNum("monitor.agent.valid.multi", Long.valueOf(2)).intValue();
		int intervalPlus = HinemosPropertyUtil.getHinemosPropertyNum("monitor.agent.valid.plus", Long.valueOf(10 * 1000)).intValue(); // 10sec
		if (interval * intervalMulti + intervalPlus > HinemosTime.currentTimeMillis() - lastLogin) {
			return true;
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
