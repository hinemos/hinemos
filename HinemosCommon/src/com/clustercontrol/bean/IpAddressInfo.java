/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.bean;

import java.net.InetAddress;

/**
 * IPアドレスに関する情報.
 * 
 * @version 6.2.0
 * @since 6.2.0
 */

public class IpAddressInfo {

	// 共通フィールド
	/** 元々のIPアドレス表記 */
	private String originIpAddress = null;

	/** プリフィックス有無 */
	private boolean havePrefix = false;

	/** IPアドレスのみの表記 */
	private String onlyIpAddress = null;

	/** InetAddressオブジェクト */
	private InetAddress inetAddress = null;

	/** IPバージョン */
	private IpVersion version = null;

	// havePrefix=true(subnet)の場合のみに利用するフィールド.
	/** サブネットに関する情報 */
	private SubnetInfo subnetInfo = null;

	// getterとsetter
	/** 元々のIPアドレス表記 */
	public String getOriginIpAddress() {
		return originIpAddress;
	}

	/** 元々のIPアドレス表記 */
	public void setOriginIpAddress(String originIpAddress) {
		this.originIpAddress = originIpAddress;
	}

	/** プリフィックス有無 */
	public boolean isHavePrefix() {
		return havePrefix;
	}

	/** プリフィックス有無 */
	public void setHavePrefix(boolean havePrefix) {
		this.havePrefix = havePrefix;
	}

	/** IPアドレスのみの表記 */
	public String getOnlyIpAddress() {
		return onlyIpAddress;
	}

	/** IPアドレスのみの表記 */
	public void setOnlyIpAddress(String onlyIpAddress) {
		this.onlyIpAddress = onlyIpAddress;
	}

	/** InetAddressオブジェクト */
	public InetAddress getInetAddress() {
		return inetAddress;
	}

	/** InetAddressオブジェクト */
	public void setInetAddress(InetAddress inetAddress) {
		this.inetAddress = inetAddress;
	}

	/** IPバージョン */
	public IpVersion getVersion() {
		return version;
	}

	/** IPバージョン */
	public void setVersion(IpVersion version) {
		this.version = version;
	}

	/** サブネットに関する情報 */
	public SubnetInfo getSubnetInfo() {
		return subnetInfo;
	}

	/** サブネットに関する情報 */
	public void setSubnetInfo(SubnetInfo subnetInfo) {
		this.subnetInfo = subnetInfo;
	}
}
