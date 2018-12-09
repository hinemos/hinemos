/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.bean;

/**
 * クライアント入力値のデータサイズの定数クラス<BR>
 * 
 * @version 4.0.0
 * @since 2.2.0
 */
public class DataRangeConstant {

	/** varchar(16) **/
	public static final int VARCHAR_16 = 16;

	/** varchar(32) **/
	public static final int VARCHAR_32 = 32;

	/** varchar(64) **/
	public static final int VARCHAR_64 = 64;

	/** varchar(80) **/
	public static final int VARCHAR_80= 80;

	/** varchar(120) **/
	public static final int VARCHAR_120= 120;

	/** varchar(128) **/
	public static final int VARCHAR_128 = 128;

	/** varchar(256) **/
	public static final int VARCHAR_256 = 256;

	/** varchar(512) **/
	public static final int VARCHAR_512 = 512;

	/** varchar(1024) **/
	public static final int VARCHAR_1024 = 1024;

	/** varchar(2048) **/
	public static final int VARCHAR_2048 = 2048;

	/** varchar(4096) **/
	public static final int VARCHAR_4096 = 4096;

	/** varchar(8192) **/
	public static final int VARCHAR_8192 = 8192;

	/** smallintの下限 **/
	public static final int SMALLINT_LOW = -32768;

	/** smallintの上限 **/
	public static final int SMALLINT_HIGH = 32767;

	/** integerの下限 -2^31(=-2147483648) **/
	public static final int INTEGER_LOW = Integer.MIN_VALUE;

	/** integerの上限 2^32-1(=2147483647) **/
	public static final int INTEGER_HIGH = Integer.MAX_VALUE;

	/** monitoreventの上限**/
	public static final int MONITOR_EVENT_MAX = 1000;

	/** monitor trap oidの上限**/
	public static final int MONITOR_TRAP_OID_MAX = 20000;
	
	/** doubleの下限 **/
	public static final double DOUBLE_LOW = -1.0E308;

	/** doubleの上限 **/
	public static final double DOUBLE_HIGH = 1.0E308;

	/** TEXT(上限なし) **/
	public static final int TEXT = 9999;

	/** ポート番号の上限 **/
	public static final int PORT_NUMBER_MAX = 65535;

	/** SNMP TRAPのgeneric trap の下限 */
	public static final int GENERIC_ID_LOW = 0;

	/** SNMP TRAPのgeneric trap の上限 */
	public static final int GENERIC_ID_HIGH = 6;

	private DataRangeConstant() {
		throw new IllegalStateException("ConstClass");
	}
}