/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.bean;

/**
 *
 * NodeFilterPropertyの定数部分を切り出した物。
 *
 */
public class NodeFilterConstant {
	/** マネージャ */
	public static final String MANAGER = "manager";
	/** ファシリティID */
	public static final String FACILITY_ID = "facilityId";
	/** ファシリティ名 */
	public static final String FACILITY_NAME = "facilityName";
	/** 説明 */
	public static final String DESCRIPTION = "description";
	/** IPアドレス v4 */
	public static final String IP_ADDRESS_V4 = "ipAddressV4";
	/** IPアドレス v6 */
	public static final String IP_ADDRESS_V6 = "ipAddressV6";
	/** OS名 */
	public static final String OS_NAME = "osName";
	/** OSリリース */
	public static final String OS_RELEASE = "osRelease";
	/** 管理者 */
	public static final String ADMINISTRATOR = "administrator";
	/** 連絡先 */
	public static final String CONTACT = "contact";

	/** ネットワーク */
	public static final String NETWORK = "network";
	/** OS */
	public static final String OS = "os";
	/** 保守 */
	public static final String MAINTENANCE = "maintenance";

	private NodeFilterConstant() {
		throw new IllegalStateException("ConstClass");
	}
}
