/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.rpa.factory.bean;

import com.clustercontrol.repository.factory.SearchNodeBySNMP;

/**
 * RPAリソース自動登録に必要な情報を格納するクラス
 *
 */
public class RpaResourceInfo {
	/** ファシリティ名 */
	private String facilityName;
	/** IPアドレス */
	private String ipAddress;
	/** ホスト名 */
	private String hostName;
	/** ノード名 */
	private String nodeName;
	/** ユーザ名 */
	private String rpaUser;
	/** RPA実行環境ID */
	private String rpaExecEnvId;

	/** ファシリティ名 */
	public String getFacilityName() {
		return facilityName;
	}
	public void setFacilityName(String facilityName) {
		this.facilityName = facilityName;
	}
	/** IPアドレス */
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	/** ホスト名 */
	public String getHostName() {
		return hostName;
	}
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}
	/** ノード名 */
	public String getNodeName() {
		return SearchNodeBySNMP.getShortName(nodeName);
	}
	public String getNodeNameOrg() {
		return nodeName;
	}
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	/** ユーザ名 */
	public String getRpaUser() {
		return rpaUser;
	}
	public void setRpaUser(String rpaUser) {
		this.rpaUser = rpaUser;
	}

	/** RPA実行環境ID */
	public String getRpaExecEnvId() {
		return rpaExecEnvId;
	}
	public void setRpaExecEnvId(String rpaExecEnvId) {
		this.rpaExecEnvId = rpaExecEnvId;
	}

	@Override
	public String toString() {
		return "RpaResourceInfo [facilityName=" + facilityName + ", ipAddress=" + ipAddress + ", hostName=" + hostName + ", nodeName=" + nodeName + ", rpaUser=" + rpaUser
				+ ", rpaExecEnvId=" + rpaExecEnvId + "]";
	}

}
