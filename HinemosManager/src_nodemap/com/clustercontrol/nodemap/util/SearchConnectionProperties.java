/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.nodemap.util;

import java.util.ArrayList;


/**
 * SNMPでL2、L3の接続状況を検出する際の定義情報のクラス<BR>
 *
 * @version 2.3.0
 * @since 2.1.2
 */
public class SearchConnectionProperties {
	public static final String DEFAULT_OID_FDB = ".1.3.6.1.2.1.17.4.3.1.1";
	public static final String DEFAULT_OID_ARP = ".1.3.6.1.2.1.4.22.1.2";

	public static ArrayList<String> getOidList(){
		ArrayList<String> oidList = new ArrayList<String>();

		oidList.add(DEFAULT_OID_FDB);
		oidList.add(DEFAULT_OID_ARP);

		return oidList;
	}
}
