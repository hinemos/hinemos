/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.bean;

/**
 * Device種別の定数クラス<BR>
 * 
 * @version 4.0.0
 * @since 2.0.0
 */
public class DeviceTypeConstant {

	/** CPU */
	public static final String DEVICE_CPU = "cpu";

	/** メモリ */
	public static final String DEVICE_MEM = "mem";

	/** NIC */
	public static final String DEVICE_NIC = "nic";
	
	/** NIC(クラウド・仮想化) */
	public static final String DEVICE_VIRT_NIC = "vnic";
	
	/** ディスク */
	public static final String DEVICE_DISK = "disk";
	
	/** ディスク(クラウド・仮想化) */
	public static final String DEVICE_VIRT_DISK = "vdisk";

	/** ファイルシステム */
	public static final String DEVICE_FILESYSTEM = "filesystem";

	/** 汎用デバイス */
	public static final String DEVICE_GENERAL = "general";

	private DeviceTypeConstant() {
		throw new IllegalStateException("ConstClass");
	}
}