
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

package com.clustercontrol.repository.factory;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.fault.FacilityNotFound;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.SnmpResponseError;
import com.clustercontrol.maintenance.util.HinemosPropertyUtil;
import com.clustercontrol.poller.bean.PollerProtocolConstant;
import com.clustercontrol.poller.impl.Snmp4jPollerImpl;
import com.clustercontrol.poller.util.DataTable;
import com.clustercontrol.poller.util.TableEntry;
import com.clustercontrol.repository.bean.DeviceTypeConstant;
import com.clustercontrol.repository.model.CollectorPlatformMstEntity;
import com.clustercontrol.repository.model.NodeCpuInfo;
import com.clustercontrol.repository.model.NodeDeviceInfo;
import com.clustercontrol.repository.model.NodeDiskInfo;
import com.clustercontrol.repository.model.NodeFilesystemInfo;
import com.clustercontrol.repository.model.NodeHostnameInfo;
import com.clustercontrol.repository.model.NodeInfo;
import com.clustercontrol.repository.model.NodeMemoryInfo;
import com.clustercontrol.repository.model.NodeNetworkInterfaceInfo;
import com.clustercontrol.repository.util.QueryUtil;
import com.clustercontrol.repository.util.SearchDeviceProperties;
import com.clustercontrol.util.HinemosTime;

/**
 * SNMPでノードのデータを作成するクラス<BR>
 * @version 4.0.0
 * @since 2.1.2
 */

public class SearchNodeBySNMP {

	private static Log m_log = LogFactory.getLog(SearchNodeBySNMP.class);

	// プラットフォーム一覧にSORARISがあるか確認する。
	private static boolean solarisFlag = false;

	static {
		try {
			CollectorPlatformMstEntity entity = QueryUtil.getCollectorPlatformMstPK("SOLARIS");
			if (entity != null) {
				solarisFlag = true; 
			}
		} catch (FacilityNotFound e) {
			m_log.debug("not found + " + e.getMessage());
		}
		m_log.info("solarisFlag=" + solarisFlag);
	}
	
	/**
	 * ノードの情報を探す。
	 * @param ipAddress
	 * @param port
	 * @param community
	 * @param version
	 * @param facilityId
	 * @return
	 * @throws UnknownHostException
	 */
	public static NodeInfo searchNode(String ipAddress, int port,
			String community, int version, String facilityId,
			String securityLevel, String user, String authPass,
			String privPass, String authProtocol, String privProtocol)
			throws UnknownHostException, SnmpResponseError, HinemosUnknown,
			FacilityNotFound {
		m_log.debug("searchNode() ipAddress = " + ipAddress + ", port = " + port + ", community = " + community + ", version = " + version);

		// 初期化
		SearchDeviceProperties pollingProp = SearchDeviceProperties.getProperties();
		int retries = pollingProp.getRetry();
		int timeout = pollingProp.getTimeOut();
		
		DataTable ret = new DataTable();

		Set<String> oidSet = pollingProp.getOidSet();
			//pollingします。
		m_log.debug("searchNode() polling start ipAddress = " + ipAddress);
		DataTable tmpDataTable = Snmp4jPollerImpl.getInstance().polling(ipAddress,
				port,
				version,
				community,
				retries,
				timeout,
				oidSet,
				securityLevel,
				user,
				authPass,
				privPass,
				authProtocol,
				privProtocol);
		m_log.debug("searchNode() polling stop ipAddress = " + ipAddress);

		// 応答がない場合エラーを返す
		if (!tmpDataTable.isNoneError()) {
			// MultipleOidsUtilsでログを出力しているためDEBUGにする
			m_log.debug("searchNode: no response ipAddress = " + ipAddress);
			throw new SnmpResponseError("no response");
		}
		
		//　取得したデータを詰め替える
		ret.putAll(tmpDataTable);

		//ポーリングした結果をつめるメソッドを呼び出します
		return stractProperty(ipAddress, port, community, version, securityLevel, user, authPass,
				privPass, authProtocol, privProtocol, ret);
	}

	/**
	 * DataTableに格納するためのEntryKeyを返すメソッド
	 *
	 * @param oidString OID
	 */
	private static String getEntryKey(String oidString){

		return PollerProtocolConstant.PROTOCOL_SNMP + "." + oidString;
	}

	/**
	 *  ノードのプロパティーを構成します。
	 * @param IPadder
	 * @param ret
	 * @param mode
	 * @param locale
	 * @return
	 * @throws UnknownHostException
	 */
	private static NodeInfo stractProperty(String ipAddress, int port, String community, int version,
			String securityLevel, String user, String authPass, String privPass, 
			String authProtocol, String privProtocol, DataTable ret) throws UnknownHostException {

		NodeInfo property = new NodeInfo();
		String facilityId = null;

		/*
		 * hinemos.propertiesにrepository.device.search.verbose=trueと書くと、
		 * ディスクIOが0でも、そのディスクがsearch対象になる。
		 * デフォルトはfalseであり、0のものはsearch対象から外れる。
		 * since 3.2.0
		 */
		boolean verbose = HinemosPropertyUtil.getHinemosPropertyBool("repository.device.search.verbose", false);

		//ノード情報"説明"の生成
		if(ret.getValue(getEntryKey(SearchDeviceProperties.getOidDescr())) != null
				&& ret.getValue(getEntryKey(SearchDeviceProperties.getOidDescr())).getValue() != null){
			if(((String)ret.getValue(getEntryKey(SearchDeviceProperties.getOidDescr())).getValue()).length() !=0){
				property.setDescription("Auto detect at " + HinemosTime.getDateString());
			}
		}

		int ipAddressVersion = 0;
		try {
			InetAddress address = InetAddress.getByName(ipAddress);

			if (address instanceof Inet4Address){
				//IPv4の場合はさらにStringをチェック
				if (ipAddress.matches(".{1,3}?\\..{1,3}?\\..{1,3}?\\..{1,3}?")){
					property.setIpAddressV4(ipAddress);
					ipAddressVersion = 4;
				}

			} else if (address instanceof Inet6Address){
				property.setIpAddressV6(ipAddress);
				ipAddressVersion = 6;
			}

			//IPアドレスのバージョン
			property.setIpAddressVersion(ipAddressVersion);
		} catch (UnknownHostException e) {
			m_log.info("stractProperty() : "
					+ e.getClass().getSimpleName() + ", " + e.getMessage());
			throw e;
		}


		//SNMP関連データの設定
		property.setSnmpPort(port);
		property.setSnmpCommunity(community);
		property.setSnmpVersion(version);
		property.setSnmpSecurityLevel(securityLevel);
		property.setSnmpUser(user);
		property.setSnmpAuthPassword(authPass);
		property.setSnmpPrivPassword(privPass);
		property.setSnmpAuthProtocol(authProtocol);
		property.setSnmpPrivProtocol(privProtocol);

		//hostname の設定
		if(ret.getValue(getEntryKey(SearchDeviceProperties.getOidName())) != null
				&& ret.getValue(getEntryKey(SearchDeviceProperties.getOidName())).getValue() != null){
			String hostname = (String)ret.getValue(getEntryKey(SearchDeviceProperties.getOidName())).getValue();
			m_log.debug("hostname=" + hostname);
			if(hostname.length() != 0){
				//hosname.domainであればhostnameだけを入力
				hostname = getShortName(hostname);

				//ファシリティID、ファシリティ名
				facilityId = hostname;
				property.setFacilityId(hostname);
				property.setFacilityName(hostname);
				
				//ホスト名、ノード名にそれぞれ設定
				ArrayList<NodeHostnameInfo> list = new ArrayList<NodeHostnameInfo> ();
				list.add(new NodeHostnameInfo(property.getFacilityId(), hostname));
				property.setNodeHostnameInfo(list);
				property.setNodeName(hostname);
			}
		} else {
			m_log.info("hostname is null");
		}


		//連絡先はsnmpd.confに書かれている内容を設定する。
		if(ret.getValue(getEntryKey(SearchDeviceProperties.getOidContact())) != null
				&& ret.getValue(getEntryKey(SearchDeviceProperties.getOidContact())).getValue() != null){
			if(((String)ret.getValue(getEntryKey(SearchDeviceProperties.getOidContact())).getValue()).length() != 0){
				property.setAdministrator((String)ret.getValue(getEntryKey(SearchDeviceProperties.getOidContact())).getValue());
			}
		}

		//プラットフォーム名は、Windows, Linux, Solaris以外はOtherとする
		String platform = "OTHER";
		if(ret.getValue(getEntryKey(SearchDeviceProperties.getOidDescr())) != null
				&& ret.getValue(getEntryKey(SearchDeviceProperties.getOidDescr())).getValue() != null){
			String description = ((String)ret.getValue(getEntryKey(SearchDeviceProperties.getOidDescr())).getValue());
			if(description.length() != 0){
				String OsName = "";

				//OSの種別はキーワードマッチで行う

				if(description.matches(".*indows.*")){
					OsName = "Windows";
					platform = "WINDOWS";
				}else if(description.matches(".*inux.*")){
					OsName = "Linux";
					platform = "LINUX";
				}else if(solarisFlag && (description.matches(".*SunOS.*") || description.matches("Solaris"))){
					// プラットフォーム一覧にSOLARISが存在するときのみ、
					// デバイスサーチやノードサーチで、プラットフォームにSOLARISが入る。
					OsName= "Solaris";
					platform = "SOLARIS";
				}else{
					if(description.indexOf(" ") != -1){
						OsName = description.substring(0,description.indexOf(" "));
					}
				}

				//OS名は上の判定ロジックを利用した値を設定
				property.setOsName(OsName);

				//OSバージョン
				property.setOsVersion(description);
			}
		}

		// プラットフォームファミリー
		// デフォルト値はOTHER
		property.setPlatformFamily(platform);

		HashMap<String, Boolean> diskNameDuplicateSet = createDiskNameDuplicateSet(ret);
		HashMap<String, Boolean> nicNameDuplicateSet = createNicNameDuplicateSet(ret);

		// ノード作成時、「デバイス」の入力項目が１つ存在するため、
		// カウンタを利用して、既存項目として入力するか、新規項目として入力するかを判定する
		int deviceCount=0;


		// Diskの情報を設定
		ArrayList<NodeDiskInfo> diskList = new ArrayList<NodeDiskInfo> ();
		for(String fullOid : ret.keySet()){
			//DISK情報で始まるか判定
			if(!fullOid.startsWith(getEntryKey(SearchDeviceProperties.getOidDiskIndex()) + ".")){
				continue;
			}
			// SearchDeviceProperties.getOidDISK_INDEX=".1.3.6.1.4.1.2021.13.15.1.1.1";
			// SearchDeviceProperties.getOidDISK_NAME =".1.3.6.1.4.1.2021.13.15.1.1.2";
			if(ret.getValue(fullOid) == null ||
					ret.getValue(fullOid).getValue() == null ||
					((Long)ret.getValue(fullOid).getValue()) == 0 ){
				continue;
			}
			m_log.debug("Find Disk : fullOid = " + fullOid);

			String i = fullOid.substring(fullOid.lastIndexOf(".") + 1);
			String disk = (String)ret.getValue(getEntryKey(SearchDeviceProperties.getOidDiskName()+"."+i)).getValue();
			Long ionRead = ret.getValue(getEntryKey(SearchDeviceProperties.getOidDiskIonRead() + "." + i)) == null ? Long.valueOf(0) :
				(Long)ret.getValue(getEntryKey(SearchDeviceProperties.getOidDiskIonRead() + "." + i)).getValue();
			Long ionWrite = ret.getValue(getEntryKey(SearchDeviceProperties.getOidDiskIonWrite() + "." + i)) == null ? Long.valueOf(0) :
				(Long)ret.getValue(getEntryKey(SearchDeviceProperties.getOidDiskIonWrite() + "." + i)).getValue();
			Long ioRead = ret.getValue(getEntryKey(SearchDeviceProperties.getOidDiskIoRead() + "." + i)) == null ? Long.valueOf(0) :
				(Long)ret.getValue(getEntryKey(SearchDeviceProperties.getOidDiskIoRead() + "." + i)).getValue();
			Long ioWrite = ret.getValue(getEntryKey(SearchDeviceProperties.getOidDiskIoWrite() + "." + i)) == null ? Long.valueOf(0) :
				(Long)ret.getValue(getEntryKey(SearchDeviceProperties.getOidDiskIoWrite() + "." + i)).getValue();
			//DISK_IOが0の物は除外します。
			if(verbose ||
					(ionRead != 0 && ionWrite != 0 && ioRead != 0 && ioWrite != 0)) {

				// デバイス名が重複している場合は、(OIDインデックス)をつける
				if (diskNameDuplicateSet != null && diskNameDuplicateSet.get(disk) != null && diskNameDuplicateSet.get(disk)){
					disk = disk + "(" + i + ")";
				}

				NodeDiskInfo diskInfo = new NodeDiskInfo(facilityId,
						((Long)ret.getValue(fullOid).getValue()).intValue(), DeviceTypeConstant.DEVICE_DISK, disk);
				// デバイス表示名
				diskInfo.setDeviceDisplayName(disk);
				// デバイス回転数(デフォルト0)
				diskInfo.setDiskRpm(0);
				deviceCount++;
				diskList.add(diskInfo);
			}
		}
		Collections.sort(diskList, new Comparator<NodeDeviceInfo>() {
			@Override
			public int compare(NodeDeviceInfo o1, NodeDeviceInfo o2) {
				int ret = 0;
				ret = o1.getDeviceType().compareTo(o2.getDeviceType());
				if (ret == 0) {
					ret = o1.getDeviceDisplayName().compareTo(o2.getDeviceDisplayName());
				}
				return ret;
			}
		}
				);
		property.setNodeDiskInfo(diskList);

		// NICの情報を設定
		ArrayList<NodeNetworkInterfaceInfo> nicList = new ArrayList<NodeNetworkInterfaceInfo> ();
		for (String fullOid : ret.keySet()) {
			//SearchDeviceProperties.getOidNIC_INDEX =".1.3.6.1.2.1.2.2.1.1";
			//SearchDeviceProperties.getOidNIC_NAME  =".1.3.6.1.2.1.2.2.1.2";
			//NIC情報で始まるか判定
			if(!fullOid.startsWith(getEntryKey(SearchDeviceProperties.getOidNicIndex()) + ".")){
				continue;
			}
			String tmpIndex = fullOid.substring(fullOid.lastIndexOf(".") + 1);
			String deviceName = "";
			if(ret.getValue(fullOid) == null ||
				ret.getValue(fullOid).getValue() == null ||
				((Long)ret.getValue(fullOid).getValue()) == 0){
				continue;
			}
			m_log.debug("Find Nic : fullOid = " + fullOid);

			deviceName = (String)ret.getValue(getEntryKey(SearchDeviceProperties.getOidNicName() + "." + tmpIndex)).getValue();

			String nicMacAddress = "";
			if (ret.getValue(getEntryKey(SearchDeviceProperties.getOidNicMacAddress() + "." + tmpIndex)) != null) {
				nicMacAddress = (String)ret.getValue(getEntryKey(SearchDeviceProperties.getOidNicMacAddress() + "." + tmpIndex)).getValue();
			}

			String nicIpAddress = "";

			if (ipAddressVersion == 4) {
				// IPv4 address from IP-MIB::ipAddressIfIndex.ipv4
				//
				// (sample)
				// # snmpwalk -c public -v 2c localhost .1.3.6.1.2.1.4.34.1.3.1.4
				// IP-MIB::ipAddressIfIndex.ipv4."127.0.0.1" = INTEGER: 1
				// IP-MIB::ipAddressIfIndex.ipv4."192.168.10.211" = INTEGER: 3
				// IP-MIB::ipAddressIfIndex.ipv4."192.168.10.255" = INTEGER: 3
				// IP-MIB::ipAddressIfIndex.ipv4."192.168.11.211" = INTEGER: 2
				// IP-MIB::ipAddressIfIndex.ipv4."192.168.11.255" = INTEGER: 2
				Pattern ipAddrV4Pattern = Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})$");
				if (ret.getValueSetStartWith(getEntryKey(SearchDeviceProperties.getOidNicIpAddressv4())) != null) {
					for (TableEntry entry : ret.getValueSetStartWith(getEntryKey(SearchDeviceProperties.getOidNicIpAddressv4()))) {
						if (entry.getValue() != null && tmpIndex.equals(((Long)entry.getValue()).toString())) {
							Matcher matcher = ipAddrV4Pattern.matcher(entry.getKey());
							if (matcher.find()) {
								// check address type (allow only unicast)
								// # snmpwalk -On -c public -v 2c 192.168.10.101 IP-MIB::ipAddressType.ipv4
								// .1.3.6.1.2.1.4.34.1.4.1.4.127.0.0.1 = INTEGER: unicast(1)
								// .1.3.6.1.2.1.4.34.1.4.1.4.192.168.10.101 = INTEGER: unicast(1)
								// .1.3.6.1.2.1.4.34.1.4.1.4.192.168.10.255 = INTEGER: broadcast(3)
								if (ret.getValue(getEntryKey(SearchDeviceProperties.getOidNicIpAddressv4Type() + "." + matcher.group(1))) != null) {
									if ((Long)ret.getValue(getEntryKey(SearchDeviceProperties.getOidNicIpAddressv4Type() + "." + matcher.group(1))).getValue() != 1) {
										continue;
									}
								}

								// set first matched address
								nicIpAddress = matcher.group(1);
								break;
							}
						}
					}
				}
			} else {
				// IPv6 Address from IP-MIB::ipAddressIfIndex.ipv6
				//
				// (sample)
				// # snmpwalk -c public -v 2c localhost .1.3.6.1.2.1.4.34.1.3.2.16
				// IP-MIB::ipAddressIfIndex.ipv6."00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:01" = INTEGER: 1
				// IP-MIB::ipAddressIfIndex.ipv6."fe:80:00:00:00:00:00:00:50:54:00:ff:fe:be:83:2a" = INTEGER: 2
				// IP-MIB::ipAddressIfIndex.ipv6."fe:80:00:00:00:00:00:00:50:54:00:ff:fe:ed:d4:3c" = INTEGER: 3
				Pattern ipAddrV6Pattern = Pattern.compile("((\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3})\\.(\\d{1,3}))$");
				if (ret.getValueSetStartWith(getEntryKey(SearchDeviceProperties.getOidNicIpAddressv6())) != null) {
					for (TableEntry entry : ret.getValueSetStartWith(getEntryKey(SearchDeviceProperties.getOidNicIpAddressv6()))) {
						if (entry.getValue() != null && tmpIndex.equals(((Long)entry.getValue()).toString())) {
							m_log.debug(entry.getKey() + " : " + entry.getValue());
							Matcher matcher = ipAddrV6Pattern.matcher(entry.getKey());
							if (matcher.find()) {
								// check address type (allow only unicast)
								// # snmpwalk -On -c public -v 2c localhost IP-MIB::ipAddressType.ipv6
								// .1.3.6.1.2.1.4.34.1.4.2.16.0.0.0.0.0.0.0.0.0.0.0.0.0.0.0.1 = INTEGER: unicast(1)
								// .1.3.6.1.2.1.4.34.1.4.2.16.254.128.0.0.0.0.0.0.80.84.0.255.254.190.131.42 = INTEGER: unicast(1)
								if (ret.getValue(getEntryKey(SearchDeviceProperties.getOidNicIpAddressv6Type() + "." + matcher.group(1))) != null) {
									if ((Long)ret.getValue(getEntryKey(SearchDeviceProperties.getOidNicIpAddressv6Type() + "." + matcher.group(1))).getValue() != 1) {
										continue;
									}
								}

								// set first matched address
								String hex = "";
								for (int i = 1; i < 17; i++) {
									hex = String.format("%02x", Integer.parseInt(matcher.group(i + 1))).toUpperCase();
									nicIpAddress += "".equals(nicIpAddress) ? hex : ":" + hex;
								}
								break;
							}
						}
					}
				}
			}

			// NICのIN/OUTが0のものは除外する。
			String key = "";
			key = getEntryKey(SearchDeviceProperties.getOidNicInOctet() + "." + tmpIndex);
			Long inOctet = ret.getValue(key) == null ? Long.valueOf(0) : (Long)ret.getValue(key).getValue();
			key = getEntryKey(SearchDeviceProperties.getOidNicOutOctet() + "." + tmpIndex);
			Long outOctet = ret.getValue(key) == null ? Long.valueOf(0) : (Long)ret.getValue(key).getValue();
			if (!verbose && inOctet == 0 && outOctet == 0) {
				continue;
			}
			// デバイス名が重複している場合は、(OIDインデックス)をつける
			if (nicNameDuplicateSet != null && nicNameDuplicateSet.get(deviceName) != null && nicNameDuplicateSet.get(deviceName)){
				deviceName = deviceName + "(." + tmpIndex + ")";
			}
			Long deviceIndex = (Long)ret.getValue(getEntryKey(SearchDeviceProperties.getOidNicIndex() + "." + tmpIndex)).getValue();
			NodeNetworkInterfaceInfo nicInfo = new NodeNetworkInterfaceInfo(facilityId, deviceIndex.intValue(), DeviceTypeConstant.DEVICE_NIC, deviceName);
			// デバイス表示名
			nicInfo.setDeviceDisplayName(deviceName);
			// デバイス名の設定
			if (deviceName.length() > 128) {
				deviceName = deviceName.substring(0, 128);
			}
			nicInfo.setDeviceName(deviceName);
			// MACアドレスの設定
			nicInfo.setNicMacAddress(nicMacAddress);
			// IPアドレスの設定
			nicInfo.setNicIpAddress(nicIpAddress);
			deviceCount++;
			nicList.add(nicInfo);
		}
		Collections.sort(nicList, new Comparator<NodeDeviceInfo>() {
			@Override
			public int compare(NodeDeviceInfo o1, NodeDeviceInfo o2) {
				int ret = 0;
				ret = o1.getDeviceType().compareTo(o2.getDeviceType());
				if (ret == 0) {
					ret = o1.getDeviceDisplayName().compareTo(o2.getDeviceDisplayName());
				}
				return ret;
			}
		}
				);
		property.setNodeNetworkInterfaceInfo(nicList);

		//ファイルシステム
		deviceCount=0;
		ArrayList<NodeFilesystemInfo> filesystemList = new ArrayList<NodeFilesystemInfo> ();
		for(String fullOid : ret.keySet()) {
			// SearchDeviceProperties.getOidFILESYSTEM_INDEX = ".1.3.6.1.2.1.25.2.3.1.1";
			// SearchDeviceProperties.getOidFILESYSTEM_NAME  = ".1.3.6.1.2.1.25.2.3.1.3";
			//DISK情報で始まるか判定
			if(!fullOid.startsWith(getEntryKey(SearchDeviceProperties.getOidFilesystemIndex()) + ".")){
				continue;
			}
			if(ret.getValue(fullOid) == null ||
				ret.getValue(fullOid).getValue() == null || 
				((Long)ret.getValue(fullOid).getValue()) == 0 ){
				continue;
			}
			m_log.debug("Find FileSystem : fullOid = " + fullOid);

			//hrStrageFixedDiskの場合のみノード情報に追加
			//.1.3.6.1.2.1.25.2.1.4←hrStrageFixedDisk
			String i = fullOid.substring(fullOid.lastIndexOf(".") + 1);
			String strageType =  ret.getValue(getEntryKey(SearchDeviceProperties.getOidFilesystemType()+"."+i)).getValue().toString();

			if(strageType.equals(".1.3.6.1.2.1.25.2.1.4")){

				//hrStorageSizeが0のものは除外する。
				String hrStorageSize = getEntryKey(SearchDeviceProperties.getOidFilesystemSize()+"."+i);
				Long storageSize = ret.getValue(hrStorageSize) == null ? Long.valueOf(0) : (Long)ret.getValue(hrStorageSize).getValue();
				if (!verbose && storageSize == 0) {
					continue;
				}

				NodeFilesystemInfo filesystem = new NodeFilesystemInfo(facilityId,
						((Long)ret.getValue(getEntryKey(SearchDeviceProperties.getOidFilesystemIndex()+"."+i)).getValue()).intValue(),
						DeviceTypeConstant.DEVICE_FILESYSTEM,
						((String)ret.getValue(getEntryKey(SearchDeviceProperties.getOidFilesystemName()+"."+i)).getValue()));

				//表示名
				filesystem.setDeviceDisplayName(convStringFilessystem(((String)ret.getValue(getEntryKey(SearchDeviceProperties.getOidFilesystemName()+"."+i)).getValue())));
				deviceCount++;
				filesystemList.add(filesystem);
			}
		}
		Collections.sort(filesystemList, new Comparator<NodeDeviceInfo>() {
			@Override
			public int compare(NodeDeviceInfo o1, NodeDeviceInfo o2) {
				int ret = 0;
				ret = o1.getDeviceType().compareTo(o2.getDeviceType());
				if (ret == 0) {
					ret = o1.getDeviceDisplayName().compareTo(o2.getDeviceDisplayName());
				}
				return ret;
			}
		}
				);
		property.setNodeFilesystemInfo(filesystemList);

		// CPU
		deviceCount=0;
		ArrayList<NodeCpuInfo> cpuList = new ArrayList<NodeCpuInfo> ();
		for(String fullOid : ret.keySet()) {
			// SearchDeviceProperties.getOidCPU_INDEX=".1.3.6.1.2.1.25.3.3.1.2";
			if(!fullOid.startsWith(getEntryKey(SearchDeviceProperties.getOidCpuIndex()) + ".") ||
				ret.getValue(fullOid) == null){
				continue;
			}
			m_log.debug("Find Cpu : fullOid = " + fullOid);

			String indexStr = fullOid.replaceFirst(getEntryKey(SearchDeviceProperties.getOidCpuIndex()) + ".","");
			m_log.debug("cpu fullOid = " + fullOid + ", index = " + indexStr);
			NodeCpuInfo cpu = new NodeCpuInfo(facilityId, Integer.valueOf(indexStr), DeviceTypeConstant.DEVICE_CPU, indexStr);
			cpu.setDeviceDisplayName(DeviceTypeConstant.DEVICE_CPU + deviceCount);
			cpu.setDeviceName(indexStr);

			cpuList.add(cpu);
			deviceCount++;
		}
		Collections.sort(cpuList, new Comparator<NodeDeviceInfo>() {
			@Override
			public int compare(NodeDeviceInfo o1, NodeDeviceInfo o2) {
				int ret = 0;
				ret = o1.getDeviceType().compareTo(o2.getDeviceType());
				if (ret == 0) {
					ret = o1.getDeviceDisplayName().compareTo(o2.getDeviceDisplayName());
				}
				return ret;
			}
		}
				);
		property.setNodeCpuInfo(cpuList);
		
		property.setNodeMemoryInfo(new ArrayList<NodeMemoryInfo>());


		m_log.debug("device search " + property.toString());
		return property;
	}

	/**
	 * SNMPで取得したファイルシステム名が16進数だった場合に文字列に変換する
	 */
	private static String convStringFilessystem(String str){
		if (str.matches("([0-9A-Fa-f]{2}\\s{0,1})+")){
			try{
				//strはなぜかASCIIが16進で入っている。
				//文字が化けて入力された場合には、変換を行う。
				char[] chars;
				short  first;
				short second;
				chars=str.toCharArray();
				StringBuffer ret = new StringBuffer();

				for(int i = 0; i<chars.length;){

					first  = (short) (chars[i] - 48); //48は"0"なので、"1"→1の変換
					second = (short)(chars[i+1]- 48);

					if(second >10){
						//16進でA-Fの場合には、さらに7を引く
						//10 ⇔ A(16進)なので("A"→65)を10にする、 10 = "A"(65) - "0"(48) - 7
						second -=7;
					}

					ret.append((char) (first*16+second));

					i+=3;//1文字は"56 "とかで構成するので、配列2つを読んで" "をスキップ
				}

				m_log.info("DeviceSearch : Filesystem Name str = " + str + " convert to " + ret.substring(0,3));
				return ret.substring(0,3);
			} catch(Exception e){
				m_log.warn("DeviceSearch : " + e.getMessage());
				return str;
			}
		}
		else {
			return str;
		}
	}


	private static HashMap<String, Boolean> createDiskNameDuplicateSet(DataTable dataTable){
		HashMap<String, Boolean> ret = new HashMap<String, Boolean>();


		int i = 1;

		while (true) {

			// SearchDeviceProperties.getOidDISK_INDEX=".1.3.6.1.4.1.2021.13.15.1.1.1";
			// SearchDeviceProperties.getOidDISK_NAME =".1.3.6.1.4.1.2021.13.15.1.1.2";
			String oidCounter = SearchDeviceProperties.getOidDiskIndex()+"."+i;

			if( dataTable.getValue(oidCounter) != null){
				if(((String)dataTable.getValue(oidCounter).getValue()).length() != 0 ){
					String disk = (String)dataTable.getValue(SearchDeviceProperties.getOidDiskName()+"."+i).getValue();

					//ramディスクは除外します。
					if(!(disk.startsWith("ram"))){

						// デバイス名
						String diskName = (String)dataTable.getValue(SearchDeviceProperties.getOidDiskName()+"."+i).getValue();

						Boolean isDuplicate = ret.get(diskName);
						if (isDuplicate == null) {
							ret.put(diskName, false);
						} else if (!isDuplicate) {
							ret.put(diskName, true);
						}
					}

					i++;

				} else {
					break;
				}
			} else {
				break;
			}
		}


		return ret;
	}

	private static HashMap<String, Boolean> createNicNameDuplicateSet(DataTable dataTable){
		HashMap<String, Boolean> ret = new HashMap<String, Boolean>();

		for (String fullOid : dataTable.keySet()) {
			//SearchDeviceProperties.getOidNIC_INDEX =".1.3.6.1.2.1.2.2.1.1";
			//SearchDeviceProperties.getOidNIC_NAME  =".1.3.6.1.2.1.2.2.1.2";

			if(fullOid.startsWith(SearchDeviceProperties.getOidNicIndex() + ".")){//NIC情報で始まるか判定

				String tmpIndex = fullOid.substring(fullOid.lastIndexOf("."));

				if( ((String)dataTable.getValue(fullOid).getValue()).length() != 0 ){

					//String nicName = convString((String)dataTable.getValue(SearchDeviceProperties.getOidNIC_NAME+tmpIndex).getValue(),isWindows);
					String nicName = (String)dataTable.getValue(SearchDeviceProperties.getOidNicName()+tmpIndex).getValue();
					m_log.info("NIC name = " + nicName);

					Boolean isDuplicate = ret.get(nicName);
					if (isDuplicate == null) {
						ret.put(nicName, false);
					} else if (!isDuplicate) {
						ret.put(nicName, true);
					}

				}
			}
		}

		return ret;
	}
	
	public static String getShortName (String fqdn) {
		String hostname = fqdn;
		if(!HinemosPropertyUtil.getHinemosPropertyBool("repository.nodename.fqdn", false) && hostname.indexOf(".") != -1){
			hostname = hostname.substring(0,hostname.indexOf("."));
		}
		return hostname;
	}
}
