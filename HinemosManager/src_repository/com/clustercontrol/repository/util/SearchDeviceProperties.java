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

package com.clustercontrol.repository.util;

import java.util.HashSet;
import java.util.Set;

import com.clustercontrol.maintenance.util.HinemosPropertyUtil;

/**
 * SNMPでノードの情報を埋める際の定義情報のクラス<BR>
 *
 * @version 2.3.0
 * @since 2.1.2
 */
public class SearchDeviceProperties {
	private static SearchDeviceProperties m_instance = null;

	private static final int DEFAULT_TIMEOUT = 5000;
	private static final int DEFAULT_RETRY = 1;
	// サブツリーなし
	private static final String DEFAULT_OID_NAME      =".1.3.6.1.2.1.1.5.0";
	private static final String DEFAULT_OID_DESCR     =".1.3.6.1.2.1.1.1.0";
	private static final String DEFAULT_OID_CONTACT   =".1.3.6.1.2.1.1.4.0";
	// サブツリーあり
	private static final String DEFAULT_OID_CPU_INDEX = ".1.3.6.1.2.1.25.3.3.1.2";
	private static final String DEFAULT_OID_DISK_INDEX =".1.3.6.1.4.1.2021.13.15.1.1.1";
	private static final String DEFAULT_OID_DISK_NAME =".1.3.6.1.4.1.2021.13.15.1.1.2";
	private static final String DEFAULT_OID_DISK_ION_READ = ".1.3.6.1.4.1.2021.13.15.1.1.3";
	private static final String DEFAULT_OID_DISK_ION_WRITE = ".1.3.6.1.4.1.2021.13.15.1.1.4";
	private static final String DEFAULT_OID_DISK_IO_READ = ".1.3.6.1.4.1.2021.13.15.1.1.5";
	private static final String DEFAULT_OID_DISK_IO_WRITE = ".1.3.6.1.4.1.2021.13.15.1.1.6";
	private static final String DEFAULT_OID_NIC_INDEX =".1.3.6.1.2.1.2.2.1.1";
	private static final String DEFAULT_OID_NIC_NAME =".1.3.6.1.2.1.2.2.1.2";
	private static final String DEFAULT_OID_NIC_MAC_ADDRESS = ".1.3.6.1.2.1.2.2.1.6";
	private static final String DEFAULT_OID_NIC_IP_ADDRESSV4 = ".1.3.6.1.2.1.4.34.1.3.1.4";
	private static final String DEFAULT_OID_NIC_IP_ADDRESSV6 = ".1.3.6.1.2.1.4.34.1.3.2.16";
	private static final String DEFAULT_OID_NIC_IP_ADDRESSV4_TYPE = ".1.3.6.1.2.1.4.34.1.4.1.4";
	private static final String DEFAULT_OID_NIC_IP_ADDRESSV6_TYPE = ".1.3.6.1.2.1.4.34.1.4.2.16";
	private static final String DEFAULT_OID_NIC_IN_OCTET = ".1.3.6.1.2.1.2.2.1.10";
	private static final String DEFAULT_OID_NIC_OUT_OCTET = ".1.3.6.1.2.1.2.2.1.16";
	private static final String DEFAULT_OID_FILESYSTEM_INDEX = ".1.3.6.1.2.1.25.2.3.1.1";
	private static final String DEFAULT_OID_FILESYSTEM_TYPE = ".1.3.6.1.2.1.25.2.3.1.2";
	private static final String DEFAULT_OID_FILESYSTEM_NAME  = ".1.3.6.1.2.1.25.2.3.1.3";
	private static final String DEFAULT_OID_FILESYSTEM_SIZE  = ".1.3.6.1.2.1.25.2.3.1.5";

	private static final String TIMEOUT_KEY       = "repository.snmp.timeout";
	private static final String RETRY_KEY         = "repository.snmp.retry";
	private static final String OID_NAME_KEY      = "repository.snmp.oid.name";
	private static final String OID_DESCR_KEY     = "repository.snmp.oid.descr";
	private static final String OID_CONTACT_KEY   = "repository.snmp.oid.contact";
	private static final String OID_CPU_INDEX_KEY = "repository.snmp.oid.cpu.index";
	private static final String OID_DISK_INDEX_KEY= "repository.snmp.oid.disk.index";
	private static final String OID_DISK_NAME_KEY = "repository.snmp.oid.disk.name";
	private static final String OID_DISK_ION_READ_KEY = "repository.snmp.oid.disk.ion.read";
	private static final String OID_DISK_ION_WRITE_KEY = "repository.snmp.oid.disk.ion.write";
	private static final String OID_DISK_IO_READ_KEY = "repository.snmp.oid.disk.io.read";
	private static final String OID_DISK_IO_WRITE_KEY = "repository.snmp.oid.disk.io.write";
	private static final String OID_NIC_INDEX_KEY = "repository.snmp.oid.nic.index";
	private static final String OID_NIC_NAME_KEY  = "repository.snmp.oid.nic.name";
	private static final String OID_NIC_MAC_ADDRESS_KEY  = "repository.snmp.oid.nic.mac.address";
	private static final String OID_NIC_IP_ADDRESSV4_KEY = "repository.snmp.oid.nic.ipv4.address";
	private static final String OID_NIC_IP_ADDRESSV6_KEY = "repository.snmp.oid.nic.ipv6.address";
	private static final String OID_NIC_IN_OCTET_KEY = "repository.snmp.oid.nic.in.octet";
	private static final String OID_NIC_OUT_OCTET_KEY = "repository.snmp.oid.nic.out.octet";
	private static final String OID_FILESYSTEM_INDEX_KEY = "repository.snmp.oid.filesystem.index";
	private static final String OID_FILESYSTEM_TYPE_KEY = "repository.snmp.oid.filesystem.type";
	private static final String OID_FILESYSTEM_NAME_KEY  = "repository.snmp.oid.filesystem.name";
	private static final String OID_FILESYSTEM_SIZE_KEY  = "repository.snmp.oid.filesystem.size";


	/**
	 * プロパティのデータセットをシングルトンで返します。
	 * @version 2.3.0
	 * @since 2.1.2
	 *
	 * @return プロパティのデータセット
	 */
	public synchronized static SearchDeviceProperties getProperties() {
		if (m_instance==null) {
			m_instance = new SearchDeviceProperties();
		}
		return m_instance;
	}

	public int getTimeOut(){
		return HinemosPropertyUtil.getHinemosPropertyNum(TIMEOUT_KEY, Long.valueOf(DEFAULT_TIMEOUT)).intValue();
	}

	public int getRetry(){
		return HinemosPropertyUtil.getHinemosPropertyNum(RETRY_KEY, Long.valueOf(DEFAULT_RETRY)).intValue();
	}

	/*
	 * 大量のOIDをSNMPのリクエストにつめると効率が落ちるため、複数に分割する。
	 * getOidListとgetListSizeは合わせて修正すること
	 */
	public Set<String> getOidSet(){
		Set<String> ret = new HashSet<String>();

		ret.add(getOidName());
		ret.add(getOidDescr());
		ret.add(getOidContact());
		ret.add(getOidCpuIndex());
		ret.add(getOidDiskIndex());
		ret.add(getOidDiskName());
		ret.add(getOidDiskIonRead());
		ret.add(getOidDiskIonWrite());
		ret.add(getOidDiskIoRead());
		ret.add(getOidDiskIoWrite());
		ret.add(getOidFilesystemIndex());
		ret.add(getOidFilesystemType());
		ret.add(getOidFilesystemName());
		ret.add(getOidFilesystemSize());
		ret.add(getOidNicIndex());
		ret.add(getOidNicName());
		ret.add(getOidNicMacAddress());
		ret.add(getOidNicIpAddressv4());
		ret.add(getOidNicIpAddressv6());
		ret.add(getOidNicIpAddressv4Type());
		ret.add(getOidNicIpAddressv6Type());
		ret.add(getOidNicInOctet());
		ret.add(getOidNicOutOctet());
		
		return ret;
	}
	
	public static String getOidName() {
		 return HinemosPropertyUtil.getHinemosPropertyStr(OID_NAME_KEY, DEFAULT_OID_NAME);
	}

	public static String getOidDescr() {
		 return HinemosPropertyUtil.getHinemosPropertyStr(OID_DESCR_KEY, DEFAULT_OID_DESCR);
	}

	public static String getOidContact() {
		 return HinemosPropertyUtil.getHinemosPropertyStr(OID_CONTACT_KEY, DEFAULT_OID_CONTACT);
	}

	public static String getOidCpuIndex() {
		 return HinemosPropertyUtil.getHinemosPropertyStr(OID_CPU_INDEX_KEY, DEFAULT_OID_CPU_INDEX);
	}

	public static String getOidDiskIndex() {
		 return HinemosPropertyUtil.getHinemosPropertyStr(OID_DISK_INDEX_KEY, DEFAULT_OID_DISK_INDEX);
	}

	public static String getOidDiskName() {
		 return HinemosPropertyUtil.getHinemosPropertyStr(OID_DISK_NAME_KEY, DEFAULT_OID_DISK_NAME);
	}

	public static String getOidDiskIonRead() {
		 return HinemosPropertyUtil.getHinemosPropertyStr(OID_DISK_ION_READ_KEY, DEFAULT_OID_DISK_ION_READ);
	}

	public static String getOidDiskIonWrite() {
		 return HinemosPropertyUtil.getHinemosPropertyStr(OID_DISK_ION_WRITE_KEY, DEFAULT_OID_DISK_ION_WRITE);
	}

	public static String getOidDiskIoRead() {
		 return HinemosPropertyUtil.getHinemosPropertyStr(OID_DISK_IO_READ_KEY, DEFAULT_OID_DISK_IO_READ);
	}

	public static String getOidDiskIoWrite() {
		 return HinemosPropertyUtil.getHinemosPropertyStr(OID_DISK_IO_WRITE_KEY, DEFAULT_OID_DISK_IO_WRITE);
	}

	public static String getOidFilesystemIndex() {
		 return HinemosPropertyUtil.getHinemosPropertyStr(OID_FILESYSTEM_INDEX_KEY, DEFAULT_OID_FILESYSTEM_INDEX );
	}

	public static String getOidFilesystemType() {
		 return HinemosPropertyUtil.getHinemosPropertyStr(OID_FILESYSTEM_TYPE_KEY , DEFAULT_OID_FILESYSTEM_TYPE);
	}

	public static String getOidFilesystemName() {
		 return HinemosPropertyUtil.getHinemosPropertyStr(OID_FILESYSTEM_NAME_KEY, DEFAULT_OID_FILESYSTEM_NAME);
	}

	public static String getOidFilesystemSize() {
		 return HinemosPropertyUtil.getHinemosPropertyStr(OID_FILESYSTEM_SIZE_KEY, DEFAULT_OID_FILESYSTEM_SIZE);
	}

	public static String getOidNicIndex() {
		 return HinemosPropertyUtil.getHinemosPropertyStr(OID_NIC_INDEX_KEY, DEFAULT_OID_NIC_INDEX);
	}

	public static String getOidNicName() {
		 return HinemosPropertyUtil.getHinemosPropertyStr(OID_NIC_NAME_KEY, DEFAULT_OID_NIC_NAME);
	}

	public static String getOidNicMacAddress() {
		 return HinemosPropertyUtil.getHinemosPropertyStr(OID_NIC_MAC_ADDRESS_KEY, DEFAULT_OID_NIC_MAC_ADDRESS);
	}

	public static String getOidNicIpAddressv4() {
		 return HinemosPropertyUtil.getHinemosPropertyStr(OID_NIC_IP_ADDRESSV4_KEY, DEFAULT_OID_NIC_IP_ADDRESSV4);
	}

	public static String getOidNicIpAddressv6() {
		 return HinemosPropertyUtil.getHinemosPropertyStr(OID_NIC_IP_ADDRESSV6_KEY, DEFAULT_OID_NIC_IP_ADDRESSV6);
	}

	public static String getOidNicIpAddressv4Type() {
		 return HinemosPropertyUtil.getHinemosPropertyStr(OID_NIC_IP_ADDRESSV4_KEY, DEFAULT_OID_NIC_IP_ADDRESSV4_TYPE);
	}

	public static String getOidNicIpAddressv6Type() {
		 return HinemosPropertyUtil.getHinemosPropertyStr(OID_NIC_IP_ADDRESSV6_KEY, DEFAULT_OID_NIC_IP_ADDRESSV6_TYPE);
	}

	public static String getOidNicInOctet() {
		 return HinemosPropertyUtil.getHinemosPropertyStr(OID_NIC_IN_OCTET_KEY, DEFAULT_OID_NIC_IN_OCTET);
	}

	public static String getOidNicOutOctet() {
		 return HinemosPropertyUtil.getHinemosPropertyStr(OID_NIC_OUT_OCTET_KEY, DEFAULT_OID_NIC_OUT_OCTET);
	}

}
