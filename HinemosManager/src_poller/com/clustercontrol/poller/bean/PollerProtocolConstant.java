/*

Copyright (C) 2009 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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
