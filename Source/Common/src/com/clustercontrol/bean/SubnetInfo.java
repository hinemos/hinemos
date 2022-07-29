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
public class SubnetInfo {

	// フィールド
	/** プリフィックスに該当するBit数(プリフィックスなしはnull) */
	private Integer prefixBit = null;

	/** サブネット内の最小アドレス */
	private InetAddress minAddress = null;

	/** サブネット内の最大アドレス */
	private InetAddress maxAddress = null;

	// getterとsetter

	/** プリフィックスに該当するBit数(プリフィックスなしはnull) */
	public Integer getPrefixBit() {
		return prefixBit;
	}

	/** プリフィックスに該当するBit数(プリフィックスなしはnull) */
	public void setPrefixBit(Integer prefixBit) {
		this.prefixBit = prefixBit;
	}

	/** サブネット内の最小アドレス */
	public InetAddress getMinAddress() {
		return minAddress;
	}

	/** サブネット内の最小アドレス */
	public void setMinAddress(InetAddress minAddress) {
		this.minAddress = minAddress;
	}

	/** サブネット内の最大アドレス */
	public InetAddress getMaxAddress() {
		return maxAddress;
	}

	/** サブネット内の最大アドレス */
	public void setMaxAddress(InetAddress maxAddress) {
		this.maxAddress = maxAddress;
	}
}
