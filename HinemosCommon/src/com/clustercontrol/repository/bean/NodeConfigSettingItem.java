/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.repository.bean;

import com.clustercontrol.util.Messages;

/**
 * 対象構成情報で収集対象IDに設定する値。それぞれ収集対象を示す。
 *   
 */
public enum NodeConfigSettingItem {

	// ホスト名情報
	HOSTNAME(Messages.getString("node.config.setting.hostname"), "hostname"),
	// OS情報
	OS(Messages.getString("node.config.setting.os"), "os"),
	// HW情報 - CPU情報
	HW_CPU(Messages.getString("node.config.setting.hw.cpu"), DeviceTypeConstant.DEVICE_CPU),
	// HW情報 - メモリ情報
	HW_MEMORY(Messages.getString("node.config.setting.hw.memory"), DeviceTypeConstant.DEVICE_MEM),
	// HW情報 - NIC情報
	HW_NIC(Messages.getString("node.config.setting.hw.nic"), DeviceTypeConstant.DEVICE_NIC),
	// HW情報 - ディスク情報
	HW_DISK(Messages.getString("node.config.setting.hw.disk"), DeviceTypeConstant.DEVICE_DISK),
	// HW情報 - ファイルシステム情報
	HW_FILESYSTEM(Messages.getString("node.config.setting.hw.filesystem"), DeviceTypeConstant.DEVICE_FILESYSTEM),
	// ノード変数情報
	NODE_VARIABLE(Messages.getString("node.config.setting.node.variable"), "variable"),
	// ネットワーク接続情報
	NETSTAT(Messages.getString("node.config.setting.netstat"), "netstat"),
	// プロセス情報
	PROCESS(Messages.getString("node.config.setting.process"), "process"),
	// パッケージ情報
	PACKAGE(Messages.getString("node.config.setting.package"), "package"),
	// 個別導入製品情報
	PRODUCT(Messages.getString("node.config.setting.product"), "product"),
	// ライセンス情報
	LICENSE(Messages.getString("node.config.setting.license"), "license"),
	// ユーザ任意情報
	CUSTOM(Messages.getString("node.config.setting.custom"), "custom");

	private final String displayName;
	private final String typeName;
	private NodeConfigSettingItem(String displayName, String typeName) {
		this.displayName = displayName;
		this.typeName = typeName;
	}

	public String displayName() {
		return this.displayName;
	}

	public String typeName() {
		return this.typeName;
	}

	/**
	 * 表示名を名称に変換する
	 * 
	 * @param displayName 表示名
	 * @return 名称
	 */
	public static String displayNameToName(String displayName) {
		String rtn = "";
		if (displayName == null || displayName.isEmpty()) {
			return rtn;
		}
		for (NodeConfigSettingItem item : values()) {
			if (item.displayName().equals(displayName)) {
				rtn = item.name();
				break;
			}
		}
		return rtn;
	}

	/**
	 * 表示名を名称に変換する
	 * 
	 * @param displayName 表示名
	 * @return 名称
	 */
	public static NodeConfigSettingItem nameToType(String name) {
		NodeConfigSettingItem rtn = null;
		if (name == null || name.isEmpty()) {
			return rtn;
		}
		for (NodeConfigSettingItem item : values()) {
			if (item.name().equals(name)) {
				rtn = item;
				break;
			}
		}
		return rtn;
	}
}
