/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.poller.bean;

import java.util.ArrayList;

/**
 * ポーラー用プロトコルの定義を定数として格納するクラスです。
 *
 * @version 3.1.0
 * @since 3.1.0
 */
public class PollerProtocolConstant {

	public static final String PROTOCOL_SNMP = "SNMP";
	public static final String PROTOCOL_WBEM = "WBEM";
	private static final String PROTOCOL_VM = "VM";

	private static final ArrayList<String> POLLER_PROTOCOLS = new ArrayList<String>();

	static {
		POLLER_PROTOCOLS.add(PROTOCOL_SNMP);
		POLLER_PROTOCOLS.add(PROTOCOL_WBEM);
		POLLER_PROTOCOLS.add(PROTOCOL_VM);
	}

	/**
	 * DataTableから値を取得するためのEntryKeyを返すメソッド
	 * 
	 * @param pollingTarget
	 */
	public static String getEntryKey(String protocol, String pollingTarget){
		return protocol + "." + pollingTarget;
	}
}
