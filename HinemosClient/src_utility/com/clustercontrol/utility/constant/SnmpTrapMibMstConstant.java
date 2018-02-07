/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.constant;

import java.util.ArrayList;


public class SnmpTrapMibMstConstant {

	//MIB一覧で使用する一般トラップとの区別用オーダーNO
	public static final int ENTERPRISE_MIB_ORDER_NO = 10;

	/**
	 * ENTERPRISEトラップのgeneric_id
	 */
	public static final int ENTERPRISE_GENERIC_ID = 6;
	
	/**
	 * GENERICトラップのspecific_id
	 */
	public static final int GENERIC_SPECIFIC_ID = 0;
	
	/**
	 * SNMPトラップエンタープライズOID。SNMPv2トラップであれば、最後のvarbindのOID。
	 */
	// public static final String SNMP_TRAP_ENTERPRISE_ID = ".1.3.6.1.6.3.1.1.4.3.0";
	
	/**
	 * SNMPトラップ値。<BR>
	 * スタンダードトラップの最後のvarbindが<code>SNMP_TRAP_ENTERPRISE_ID</code>ではない場合に、使用されます。
	 */
	public static final String SNMP_TRAPS = ".1.3.6.1.6.3.1.1.5";
	
	/**
	 * スタンダードトラップ一覧。
	 */
	public static final ArrayList<String> GENERIC_TRAPS;

	// スタンダードトラップ一覧作成。SNMPv2処理にて使用。
	static {
		GENERIC_TRAPS = new ArrayList<String>();
		GENERIC_TRAPS.add(".1.3.6.1.6.3.1.1.5.1"); // coldStart
		GENERIC_TRAPS.add(".1.3.6.1.6.3.1.1.5.2"); // warmStart
		GENERIC_TRAPS.add(".1.3.6.1.6.3.1.1.5.3"); // linkDown
		GENERIC_TRAPS.add(".1.3.6.1.6.3.1.1.5.4"); // linkUp
		GENERIC_TRAPS.add(".1.3.6.1.6.3.1.1.5.5"); // authenticationFailure
		GENERIC_TRAPS.add(".1.3.6.1.6.3.1.1.5.6"); // egpNeighborLoss
	}
	
	/**
	 * SNMPトラップOIDのvarbindのインデックス。
	 */
	public static final int SNMP_TRAP_OID_INDEX = 1;
	
	/**
	 * OIDのセパレータ（ドット）。
	 */
	public static final char DOT_CHAR = '.';
	

	
	/** MIBファイルのヘッダタイトル */
	public static final String HEADER = "HEADER = ";
	
	/** MIBファイルのフッタタイトル */
	public static final String FOOTER = "FOOTER = ";
	
	/** logmsgの一部 */
	public static final String LOGMSG = " received. ";


}
