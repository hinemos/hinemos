/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import com.clustercontrol.util.Messages;

/**
 * 構成情報検索処理で使用する比較方法を定数として格納するクラス<BR>
 * 
 * @version 6.2.0
 */
public enum NodeConfigFilterItem {

	/** ----- ホスト名 ----- */
	HOSTNAME(NodeConfigSettingItem.HOSTNAME, Messages.getString("host.name"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 0),

	/** ----- OS情報 ----- */
	OS_NAME(NodeConfigSettingItem.OS, Messages.getString("os.name"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 0),
	OS_RELEASE(NodeConfigSettingItem.OS, Messages.getString("os.release"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 1),
	OS_VERSION(NodeConfigSettingItem.OS, Messages.getString("os.version"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 2),
	OS_CHARACTER_SET(NodeConfigSettingItem.OS, Messages.getString("character.set"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 3),
	OS_STARTUP_DATE_TIME(NodeConfigSettingItem.OS, Messages.getString("os.startup.date.time"), NodeConfigFilterDataType.DATETIME, 4),

	/** ----- デバイス項目(CPU) ----- */
	CPU_DEVICE_NAME(NodeConfigSettingItem.HW_CPU, Messages.getString("device.name"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 0),
	CPU_DEVICE_DISPLAY_NAME(NodeConfigSettingItem.HW_CPU, Messages.getString("device.display.name"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 1),
	CPU_DEVICE_SIZE(NodeConfigSettingItem.HW_CPU, Messages.getString("device.size"), NodeConfigFilterDataType.INTEGER, 2),
	CPU_DEVICE_SIZE_UNIT(NodeConfigSettingItem.HW_CPU, Messages.getString("device.size.unit"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 3),
	CPU_DEVICE_DESCRIPTION(NodeConfigSettingItem.HW_CPU, Messages.getString("description"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 4),
	CPU_CORE_COUNT(NodeConfigSettingItem.HW_CPU, Messages.getString("cpu.core.count"), NodeConfigFilterDataType.INTEGER, 5),
	CPU_THREAD_COUNT(NodeConfigSettingItem.HW_CPU, Messages.getString("cpu.thread.count"), NodeConfigFilterDataType.INTEGER, 6),
	CPU_CLOCK_COUNT(NodeConfigSettingItem.HW_CPU, Messages.getString("cpu.clock.count"), NodeConfigFilterDataType.INTEGER, 7),

	/** ----- デバイス項目(MEM) ----- */
	MEMORY_DEVICE_NAME(NodeConfigSettingItem.HW_MEMORY, Messages.getString("device.name"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 0),
	MEMORY_DEVICE_DISPLAY_NAME(NodeConfigSettingItem.HW_MEMORY, Messages.getString("device.display.name"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 1),
	MEMORY_DEVICE_SIZE(NodeConfigSettingItem.HW_MEMORY, Messages.getString("device.size"), NodeConfigFilterDataType.INTEGER, 2),
	MEMORY_DEVICE_SIZE_UNIT(NodeConfigSettingItem.HW_MEMORY, Messages.getString("device.size.unit"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 3),
	MEMORY_DEVICE_DESCRIPTION(NodeConfigSettingItem.HW_MEMORY, Messages.getString("description"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 4),

	/** ----- デバイス項目(NIC) ----- */
	NIC_DEVICE_NAME(NodeConfigSettingItem.HW_NIC, Messages.getString("device.name"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 0),
	NIC_DEVICE_DISPLAY_NAME(NodeConfigSettingItem.HW_NIC, Messages.getString("device.display.name"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 1),
	NIC_DEVICE_SIZE(NodeConfigSettingItem.HW_NIC, Messages.getString("device.size"), NodeConfigFilterDataType.INTEGER, 2),
	NIC_DEVICE_SIZE_UNIT(NodeConfigSettingItem.HW_NIC, Messages.getString("device.size.unit"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 3),
	NIC_DEVICE_DESCRIPTION(NodeConfigSettingItem.HW_NIC, Messages.getString("description"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 4),
	NIC_IP_ADDRESS(NodeConfigSettingItem.HW_NIC, Messages.getString("nic.ip.address"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 5),
	NIC_MAC_ADDRESS(NodeConfigSettingItem.HW_NIC, Messages.getString("nic.mac.address"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 6),

	/** ----- デバイス項目(DISK) ----- */
	DISK_DEVICE_NAME(NodeConfigSettingItem.HW_DISK, Messages.getString("device.name"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 0),
	DISK_DEVICE_DISPLAY_NAME(NodeConfigSettingItem.HW_DISK, Messages.getString("device.display.name"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 1),
	DISK_DEVICE_SIZE(NodeConfigSettingItem.HW_DISK, Messages.getString("device.size"), NodeConfigFilterDataType.INTEGER, 2),
	DISK_DEVICE_SIZE_UNIT(NodeConfigSettingItem.HW_DISK, Messages.getString("device.size.unit"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 3),
	DISK_DEVICE_DESCRIPTION(NodeConfigSettingItem.HW_DISK, Messages.getString("description"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 4),
	DISK_RPM(NodeConfigSettingItem.HW_DISK, Messages.getString("disk.rpm"), NodeConfigFilterDataType.INTEGER, 5),

	/** ----- デバイス項目(ファイルシステム) ----- */
	FILESYSTEM_DEVICE_NAME(NodeConfigSettingItem.HW_FILESYSTEM, Messages.getString("device.name"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 0),
	FILESYSTEM_DEVICE_DISPLAY_NAME(NodeConfigSettingItem.HW_FILESYSTEM, Messages.getString("device.display.name"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 1),
	FILESYSTEM_DEVICE_SIZE(NodeConfigSettingItem.HW_FILESYSTEM, Messages.getString("device.size"), NodeConfigFilterDataType.INTEGER, 2),
	FILESYSTEM_DEVICE_SIZE_UNIT(NodeConfigSettingItem.HW_FILESYSTEM, Messages.getString("device.size.unit"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 3),
	FILESYSTEM_DEVICE_DESCRIPTION(NodeConfigSettingItem.HW_FILESYSTEM, Messages.getString("description"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 4),
	FILESYSTEM_TYPE(NodeConfigSettingItem.HW_FILESYSTEM, Messages.getString("file.system.type"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 5),

	/** ----- ノード変数 ----- */
	NODE_VARIABLE_NAME(NodeConfigSettingItem.NODE_VARIABLE, Messages.getString("node.variable.name"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 0),
	NODE_VARIABLE_VALUE(NodeConfigSettingItem.NODE_VARIABLE, Messages.getString("node.variable.value"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 1),

	/** ----- ネットワーク接続 ----- */
	NETSTAT_PROTOCOL(NodeConfigSettingItem.NETSTAT, Messages.getString("node.netstat.protocol"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 0),
	NETSTAT_LOCAL_IP_ADDRESS(NodeConfigSettingItem.NETSTAT, Messages.getString("node.netstat.local.ip.address"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 1),
	NETSTAT_LOCAL_PORT(NodeConfigSettingItem.NETSTAT, Messages.getString("node.netstat.local.port"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 2),
	NETSTAT_FOREIGN_IP_ADDRESS(NodeConfigSettingItem.NETSTAT, Messages.getString("node.netstat.foreign.ip.address"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 3),
	NETSTAT_FOREIGN_PORT(NodeConfigSettingItem.NETSTAT, Messages.getString("node.netstat.foreign.port"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 4),
	NETSTAT_PROCESS_NAME(NodeConfigSettingItem.NETSTAT, Messages.getString("node.netstat.process.name"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 5),
	NETSTAT_PID(NodeConfigSettingItem.NETSTAT, Messages.getString("node.netstat.pid"), NodeConfigFilterDataType.INTEGER_ONLYEQUAL, 6),
	NETSTAT_STATUS(NodeConfigSettingItem.NETSTAT, Messages.getString("node.netstat.status"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 7),

	/** ----- プロセス ----- */
	PROCESS_NAME(NodeConfigSettingItem.PROCESS, Messages.getString("node.process.name"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 0),
	PROCESS_PID(NodeConfigSettingItem.PROCESS, Messages.getString("node.process.pid"), NodeConfigFilterDataType.INTEGER_ONLYEQUAL, 1),
	PROCESS_PATH(NodeConfigSettingItem.PROCESS, Messages.getString("node.process.path"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 2),
	PROCESS_EXEC_USER(NodeConfigSettingItem.PROCESS, Messages.getString("node.process.exec.user"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 3),
	PROCESS_STARTUP_DATE_TIME(NodeConfigSettingItem.PROCESS, Messages.getString("node.process.startup.date.time"), NodeConfigFilterDataType.DATETIME, 4),

	/** ----- パッケージ ----- */
	PACKAGE_NAME(NodeConfigSettingItem.PACKAGE, Messages.getString("node.package.name"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 0),
	PACKAGE_VERSION(NodeConfigSettingItem.PACKAGE, Messages.getString("node.package.version"), NodeConfigFilterDataType.STRING_VERSION, 1),
	PACKAGE_RELEASE(NodeConfigSettingItem.PACKAGE, Messages.getString("node.package.release"), NodeConfigFilterDataType.STRING_VERSION, 2),
	PACKAGE_INSTALL_DATE(NodeConfigSettingItem.PACKAGE, Messages.getString("node.package.install.date"), NodeConfigFilterDataType.DATETIME, 3),
	PACKAGE_VENDOR(NodeConfigSettingItem.PACKAGE, Messages.getString("node.package.vendor"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 4),
	PACKAGE_ARCHITECTURE(NodeConfigSettingItem.PACKAGE, Messages.getString("node.package.architecture"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 5),

	/** ----- 個別導入製品 ----- */
	PRODUCT_NAME(NodeConfigSettingItem.PRODUCT, Messages.getString("node.product.name"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 0),
	PRODUCT_VERSION(NodeConfigSettingItem.PRODUCT, Messages.getString("node.product.version"), NodeConfigFilterDataType.STRING_VERSION, 1),
	PRODUCT_PATH(NodeConfigSettingItem.PRODUCT, Messages.getString("node.product.path"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 2),

	/** ----- ライセンス ----- */
	LICENSE_PRODUCT_NAME(NodeConfigSettingItem.LICENSE, Messages.getString("node.license.product.name"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 0),
	LICENSE_VENDOR(NodeConfigSettingItem.LICENSE, Messages.getString("node.license.vendor"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 0),
	LICENSE_VENDOR_CONTACT(NodeConfigSettingItem.LICENSE, Messages.getString("node.license.vendor.contact"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 0),
	LICENSE_SERIAL_NUMBER(NodeConfigSettingItem.LICENSE, Messages.getString("node.license.serial.number"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 0),
	LICENSE_COUNT(NodeConfigSettingItem.LICENSE, Messages.getString("node.license.count"), NodeConfigFilterDataType.INTEGER, 0),
	LICENSE_EXPIRATION_DATE(NodeConfigSettingItem.LICENSE, Messages.getString("node.license.expiration.date"), NodeConfigFilterDataType.DATETIME, 1),

	/** ----- ユーザ任意情報 ----- */
	CUSTOM_DISPLAY_NAME(NodeConfigSettingItem.CUSTOM, Messages.getString("node.custom.name"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 0),
	CUSTOM_VALUE(NodeConfigSettingItem.CUSTOM, Messages.getString("value"), NodeConfigFilterDataType.STRING_ONLYEQUAL, 1);

	// 構成情報の項目
	private static Map<NodeConfigSettingItem, List<NodeConfigFilterItem>> valueMap;
	// 構成情報の分類
	private final NodeConfigSettingItem nodeConfigSettingItem;
	// 表示文字列
	private final String displayName;
	// データ種別
	private final NodeConfigFilterDataType dataType;
	// 表示順
	private final Integer orderNo;

	static {
		final Hashtable<NodeConfigSettingItem, List<NodeConfigFilterItem>> _valueMap = new Hashtable<>();
		for (NodeConfigFilterItem item : values()) {
			if (!_valueMap.containsKey(item.nodeConfigSettingItem)) {
				_valueMap.put(item.nodeConfigSettingItem, new ArrayList<NodeConfigFilterItem>());
			}
			_valueMap.get(item.nodeConfigSettingItem).add(item);
		}
		for (Map.Entry<NodeConfigSettingItem, List<NodeConfigFilterItem>> entry : _valueMap.entrySet()) {
			Collections.sort(entry.getValue(), new Comparator<NodeConfigFilterItem>() {
				@Override
				public int compare(NodeConfigFilterItem info1, NodeConfigFilterItem info2) {
					return info1.orderNo.compareTo(info2.orderNo);
				}
			});			
		}
		valueMap = Collections.unmodifiableMap(_valueMap);
	}

	/**
	 * コンストラクタ
	 * 
	 * @param nodeConfigSettingItem 構成情報の種別
	 * @param displayName 表示文字列
	 * @param dataType データ型
	 */
	private NodeConfigFilterItem(
			NodeConfigSettingItem nodeConfigSettingItem, String displayName, NodeConfigFilterDataType dataType, Integer orderNo) {
		this.nodeConfigSettingItem = nodeConfigSettingItem;
		this.displayName = displayName;
		this.dataType = dataType;
		this.orderNo = orderNo;
	}

	/**
	 * 構成情報の種別に対応した項目を返す
	 * 
	 * @param nodeConfigSettingItem 構成情報の種別
	 * @return 構成情報の項目
	 */
	public static List<NodeConfigFilterItem> getTargetItemList(NodeConfigSettingItem nodeConfigSettingItem) {
		List<NodeConfigFilterItem> itemList = new ArrayList<>();
		if (valueMap.containsKey(nodeConfigSettingItem)) {
			itemList.addAll(valueMap.get(nodeConfigSettingItem));
		}
		return itemList;
	}

	/**
	 * 構成情報の分類を返す
	 * 
	 * @return 構成情報の分類
	 */
	public NodeConfigSettingItem nodeConfigSettingItem() {
		return nodeConfigSettingItem;
	}

	/**
	 * 表示文字列を返す
	 * 
	 * @return 表示文字列
	 */
	public String displayName() {
		return displayName;
	}

	/**
	 * データ種別を返す
	 * 
	 * @return データ種別
	 */
	public NodeConfigFilterDataType dataType() {
		return dataType;
	}
}