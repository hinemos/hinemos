/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.util;

import java.util.HashSet;
import java.util.Set;

import com.clustercontrol.commons.util.HinemosPropertyCommon;

/**
 * SNMPでノードの情報を埋める際の定義情報のクラス<BR>
 *
 * @version 2.3.0
 * @since 2.1.2
 */
public class SearchDeviceProperties {
	private static SearchDeviceProperties m_instance = null;

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
		return HinemosPropertyCommon.repository_snmp_timeout.getIntegerValue();
	}

	public int getRetry(){
		return HinemosPropertyCommon.repository_snmp_retry.getIntegerValue();
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
		 return HinemosPropertyCommon.repository_snmp_oid_name.getStringValue();
	}

	public static String getOidDescr() {
		 return HinemosPropertyCommon.repository_snmp_oid_descr.getStringValue();
	}

	public static String getOidContact() {
		 return HinemosPropertyCommon.repository_snmp_oid_contact.getStringValue();
	}

	public static String getOidCpuIndex() {
		 return HinemosPropertyCommon.repository_snmp_oid_cpu_index.getStringValue();
	}

	public static String getOidDiskIndex() {
		 return HinemosPropertyCommon.repository_snmp_oid_disk_index.getStringValue();
	}

	public static String getOidDiskName() {
		 return HinemosPropertyCommon.repository_snmp_oid_disk_name.getStringValue();
	}

	public static String getOidDiskIonRead() {
		 return HinemosPropertyCommon.repository_snmp_oid_disk_ion_read.getStringValue();
	}

	public static String getOidDiskIonWrite() {
		 return HinemosPropertyCommon.repository_snmp_oid_disk_ion_write.getStringValue();
	}

	public static String getOidDiskIoRead() {
		 return HinemosPropertyCommon.repository_snmp_oid_disk_io_read.getStringValue();
	}

	public static String getOidDiskIoWrite() {
		 return HinemosPropertyCommon.repository_snmp_oid_disk_io_write.getStringValue();
	}

	public static String getOidFilesystemIndex() {
		 return HinemosPropertyCommon.repository_snmp_oid_filesystem_index.getStringValue();
	}

	public static String getOidFilesystemType() {
		 return HinemosPropertyCommon.repository_snmp_oid_filesystem_type.getStringValue();
	}

	public static String getOidFilesystemName() {
		 return HinemosPropertyCommon.repository_snmp_oid_filesystem_name.getStringValue();
	}

	public static String getOidFilesystemSize() {
		 return HinemosPropertyCommon.repository_snmp_oid_filesystem_size.getStringValue();
	}

	public static String getOidNicIndex() {
		 return HinemosPropertyCommon.repository_snmp_oid_nic_index.getStringValue();
	}

	public static String getOidNicName() {
		 return HinemosPropertyCommon.repository_snmp_oid_nic_name.getStringValue();
	}

	public static String getOidNicMacAddress() {
		 return HinemosPropertyCommon.repository_snmp_oid_nic_mac_address.getStringValue();
	}

	public static String getOidNicIpAddressv4() {
		 return HinemosPropertyCommon.repository_snmp_oid_nic_ipv4_address.getStringValue();
	}

	public static String getOidNicIpAddressv6() {
		 return HinemosPropertyCommon.repository_snmp_oid_nic_ipv6_address.getStringValue();
	}

	public static String getOidNicIpAddressv4Type() {
		 return HinemosPropertyCommon.repository_snmp_oid_nic_ipv4_address_type.getStringValue();
	}

	public static String getOidNicIpAddressv6Type() {
		 return HinemosPropertyCommon.repository_snmp_oid_nic_ipv6_address_type.getStringValue();
	}

	public static String getOidNicInOctet() {
		 return HinemosPropertyCommon.repository_snmp_oid_nic_in_octet.getStringValue();
	}

	public static String getOidNicOutOctet() {
		 return HinemosPropertyCommon.repository_snmp_oid_nic_out_octet.getStringValue();
	}

}
