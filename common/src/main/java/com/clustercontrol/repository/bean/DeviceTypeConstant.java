/*

Copyright (C) 2006 NTT DATA Corporation

This program is free software; you can redistribute it and/or
Modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, version 2.

This program is distributed in the hope that it will be
useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
PURPOSE.  See the GNU General Public License for more details.

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

}