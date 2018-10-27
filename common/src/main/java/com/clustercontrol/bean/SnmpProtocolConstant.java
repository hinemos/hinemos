/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.bean;

import java.util.ArrayList;
import java.util.List;


/**
 * SNMPプロトコルの定義クラス<BR>
 *
 * @version 5.0.0
 * @since 5.0.0
 */
public class SnmpProtocolConstant {
	/**  MD5 */
	public static final String  MD5 = "MD5";

	/**  SHA */
	public static final String  SHA = "SHA";

	/**  DES */
	public static final String  DES = "DES";

	/**  AES */
	public static final String  AES = "AES";

	// 認証プロトコル
	public static List<String> getAuthProtocol() {
		ArrayList<String> list = new ArrayList<String>();
		list.add(MD5);
		list.add(SHA);
		return list;
	}
	
	// 暗号化プロトコル
	public static List<String> getPrivProtocol() {
		ArrayList<String> list = new ArrayList<String>();
		list.add(DES);
		list.add(AES);
		return list;
	}
	
	
}