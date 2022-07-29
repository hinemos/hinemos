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

	/** SHA-224 */
	public static final String SHA224 = "SHA-224";

	/** SHA-256 */
	public static final String SHA256 = "SHA-256";

	/** SHA-384 */
	public static final String SHA384 = "SHA-384";

	/** SHA-512 */
	public static final String SHA512 = "SHA-512";

	/**  DES */
	public static final String  DES = "DES";

	/**  AES */
	public static final String  AES = "AES";

	/** AES-192 */
	public static final String AES192 = "AES-192";

	/** AES-256 */
	public static final String AES256 = "AES-256";

	// 認証プロトコル
	public static List<String> getAuthProtocol() {
		ArrayList<String> list = new ArrayList<String>();
		list.add(MD5);
		list.add(SHA);
		list.add(SHA224);
		list.add(SHA256);
		list.add(SHA384);
		list.add(SHA512);
		return list;
	}
	
	// 暗号化プロトコル
	public static List<String> getPrivProtocol() {
		ArrayList<String> list = new ArrayList<String>();
		list.add(DES);
		list.add(AES);
		list.add(AES192);
		list.add(AES256);
		return list;
	}
	
	
}