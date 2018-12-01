/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.bean;

/**
 * SNMPバージョンの定義クラス<BR>
 *
 * @version 2.0.0
 * @since 2.0.0
 */
public class SnmpVersionConstant {

	/*
	 * TYPE_V1,2,3は、 org.snmp4j.mp.SnmpConstants に倣っています。
	 */

	/** v1 */
	public static final int TYPE_V1 = 0;
	public static final String STRING_V1 = "1";
	
	/** v2c */
	public static final int TYPE_V2 = 1;
	public static final String STRING_V2 = "2c";

	/** v3 */
	public static final int TYPE_V3 = 3;
	public static final String STRING_V3 = "3";

	public static String typeToString(Integer type) {
		if (type == TYPE_V1) {
			return STRING_V1;
		} else if (type == TYPE_V2) {
			return STRING_V2;
		} else if (type == TYPE_V3) {
			return STRING_V3;
		}
		return "";
	}
	
	public static Integer stringToType(String str) {
		if (STRING_V1.equals(str)) {
			return TYPE_V1;
		} else if (STRING_V2.equals(str)) {
			return TYPE_V2;
		} else if (STRING_V3.equals(str)) {
			return TYPE_V3;
		}
		return -1;
	}

	private SnmpVersionConstant() {
		throw new IllegalStateException("ConstClass");
	}
}