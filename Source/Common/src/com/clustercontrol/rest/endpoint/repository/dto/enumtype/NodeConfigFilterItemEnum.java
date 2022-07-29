/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */
package com.clustercontrol.rest.endpoint.repository.dto.enumtype;

import com.clustercontrol.repository.bean.NodeConfigFilterItem;
import com.clustercontrol.rest.dto.EnumDto;

public enum NodeConfigFilterItemEnum implements EnumDto<String> {

	/** ----- ホスト名 ----- */
	// @formatter:off
	HOSTNAME(NodeConfigFilterItem.HOSTNAME.name()),
	// @formatter:on

	/** ----- OS情報 ----- */
	// @formatter:off
	OS_NAME(NodeConfigFilterItem.OS_NAME.name()),
	OS_RELEASE(NodeConfigFilterItem.OS_RELEASE.name()),
	OS_VERSION(NodeConfigFilterItem.OS_VERSION.name()),
	OS_CHARACTER_SET(NodeConfigFilterItem.OS_CHARACTER_SET.name()),
	OS_STARTUP_DATE_TIME(NodeConfigFilterItem.OS_STARTUP_DATE_TIME.name()),
	// @formatter:on

	/** ----- デバイス項目(CPU) ----- */
	// @formatter:off
	CPU_DEVICE_NAME(NodeConfigFilterItem.CPU_DEVICE_NAME.name()),
	CPU_DEVICE_DISPLAY_NAME(NodeConfigFilterItem.CPU_DEVICE_DISPLAY_NAME.name()),
	CPU_DEVICE_SIZE(NodeConfigFilterItem.CPU_DEVICE_SIZE.name()),
	CPU_DEVICE_SIZE_UNIT(NodeConfigFilterItem.CPU_DEVICE_SIZE_UNIT.name()),
	CPU_DEVICE_DESCRIPTION(NodeConfigFilterItem.CPU_DEVICE_DESCRIPTION.name()),
	CPU_CORE_COUNT(NodeConfigFilterItem.CPU_CORE_COUNT.name()),
	CPU_THREAD_COUNT(NodeConfigFilterItem.CPU_THREAD_COUNT.name()),
	CPU_CLOCK_COUNT(NodeConfigFilterItem.CPU_CLOCK_COUNT.name()),
	// @formatter:on

	/** ----- デバイス項目(MEM) ----- */
	// @formatter:off
	MEMORY_DEVICE_NAME(NodeConfigFilterItem.MEMORY_DEVICE_NAME.name()),
	MEMORY_DEVICE_DISPLAY_NAME(NodeConfigFilterItem.MEMORY_DEVICE_DISPLAY_NAME.name()),
	MEMORY_DEVICE_SIZE(NodeConfigFilterItem.MEMORY_DEVICE_SIZE.name()),
	MEMORY_DEVICE_SIZE_UNIT(NodeConfigFilterItem.MEMORY_DEVICE_SIZE_UNIT.name()),
	MEMORY_DEVICE_DESCRIPTION(NodeConfigFilterItem.MEMORY_DEVICE_DESCRIPTION.name()),
	// @formatter:on

	/** ----- デバイス項目(NIC) ----- */
	// @formatter:off
	NIC_DEVICE_NAME(NodeConfigFilterItem.NIC_DEVICE_NAME.name()),
	NIC_DEVICE_DISPLAY_NAME(NodeConfigFilterItem.NIC_DEVICE_DISPLAY_NAME.name()),
	NIC_DEVICE_SIZE(NodeConfigFilterItem.NIC_DEVICE_SIZE.name()),
	NIC_DEVICE_SIZE_UNIT(NodeConfigFilterItem.NIC_DEVICE_SIZE_UNIT.name()),
	NIC_DEVICE_DESCRIPTION(NodeConfigFilterItem.NIC_DEVICE_DESCRIPTION.name()),
	NIC_IP_ADDRESS(NodeConfigFilterItem.NIC_IP_ADDRESS.name()),
	NIC_MAC_ADDRESS(NodeConfigFilterItem.NIC_MAC_ADDRESS.name()),
	// @formatter:on

	/** ----- デバイス項目(DISK) ----- */
	// @formatter:off
	DISK_DEVICE_NAME(NodeConfigFilterItem.DISK_DEVICE_NAME.name()),
	DISK_DEVICE_DISPLAY_NAME(NodeConfigFilterItem.DISK_DEVICE_DISPLAY_NAME.name()),
	DISK_DEVICE_SIZE(NodeConfigFilterItem.DISK_DEVICE_SIZE.name()),
	DISK_DEVICE_SIZE_UNIT(NodeConfigFilterItem.DISK_DEVICE_SIZE_UNIT.name()),
	DISK_DEVICE_DESCRIPTION(NodeConfigFilterItem.DISK_DEVICE_DESCRIPTION.name()),
	DISK_RPM(NodeConfigFilterItem.DISK_RPM.name()),
	// @formatter:on

	/** ----- デバイス項目(ファイルシステム) ----- */
	// @formatter:off
	FILESYSTEM_DEVICE_NAME(NodeConfigFilterItem.FILESYSTEM_DEVICE_NAME.name()),
	FILESYSTEM_DEVICE_DISPLAY_NAME(NodeConfigFilterItem.FILESYSTEM_DEVICE_DISPLAY_NAME.name()),
	FILESYSTEM_DEVICE_SIZE(NodeConfigFilterItem.FILESYSTEM_DEVICE_SIZE.name()),
	FILESYSTEM_DEVICE_SIZE_UNIT(NodeConfigFilterItem.FILESYSTEM_DEVICE_SIZE_UNIT.name()),
	FILESYSTEM_DEVICE_DESCRIPTION(NodeConfigFilterItem.FILESYSTEM_DEVICE_DESCRIPTION.name()),
	FILESYSTEM_TYPE(NodeConfigFilterItem.FILESYSTEM_TYPE.name()),
	// @formatter:on

	/** ----- ノード変数 ----- */
	// @formatter:off
	NODE_VARIABLE_NAME(NodeConfigFilterItem.NODE_VARIABLE_NAME.name()),
	NODE_VARIABLE_VALUE(NodeConfigFilterItem.NODE_VARIABLE_VALUE.name()),
	// @formatter:on

	/** ----- ネットワーク接続 ----- */
	// @formatter:off
	NETSTAT_PROTOCOL(NodeConfigFilterItem.NETSTAT_PROTOCOL.name()),
	NETSTAT_LOCAL_IP_ADDRESS(NodeConfigFilterItem.NETSTAT_LOCAL_IP_ADDRESS.name()),
	NETSTAT_LOCAL_PORT(NodeConfigFilterItem.NETSTAT_LOCAL_PORT.name()),
	NETSTAT_FOREIGN_IP_ADDRESS(NodeConfigFilterItem.NETSTAT_FOREIGN_IP_ADDRESS.name()),
	NETSTAT_FOREIGN_PORT(NodeConfigFilterItem.NETSTAT_FOREIGN_PORT.name()),
	NETSTAT_PROCESS_NAME(NodeConfigFilterItem.NETSTAT_PROCESS_NAME.name()),
	NETSTAT_PID(NodeConfigFilterItem.NETSTAT_PID.name()),
	NETSTAT_STATUS(NodeConfigFilterItem.NETSTAT_STATUS.name()),
	// @formatter:on

	/** ----- プロセス ----- */
	// @formatter:off
	PROCESS_NAME(NodeConfigFilterItem.PROCESS_NAME.name()),
	PROCESS_PID(NodeConfigFilterItem.PROCESS_PID.name()),
	PROCESS_PATH(NodeConfigFilterItem.PROCESS_PATH.name()),
	PROCESS_EXEC_USER(NodeConfigFilterItem.PROCESS_EXEC_USER.name()),
	PROCESS_STARTUP_DATE_TIME(NodeConfigFilterItem.PROCESS_STARTUP_DATE_TIME.name()),
	// @formatter:on

	/** ----- パッケージ ----- */
	// @formatter:off
	PACKAGE_NAME(NodeConfigFilterItem.PACKAGE_NAME.name()),
	PACKAGE_VERSION(NodeConfigFilterItem.PACKAGE_VERSION.name()),
	PACKAGE_RELEASE(NodeConfigFilterItem.PACKAGE_RELEASE.name()),
	PACKAGE_INSTALL_DATE(NodeConfigFilterItem.PACKAGE_INSTALL_DATE.name()),
	PACKAGE_VENDOR(NodeConfigFilterItem.PACKAGE_VENDOR.name()),
	PACKAGE_ARCHITECTURE(NodeConfigFilterItem.PACKAGE_ARCHITECTURE.name()),
	// @formatter:on

	/** ----- 個別導入製品 ----- */
	// @formatter:off
	PRODUCT_NAME(NodeConfigFilterItem.PRODUCT_NAME.name()),
	PRODUCT_VERSION(NodeConfigFilterItem.PRODUCT_VERSION.name()),
	PRODUCT_PATH(NodeConfigFilterItem.PRODUCT_PATH.name()),
	// @formatter:on

	/** ----- ライセンス ----- */
	// @formatter:off
	LICENSE_PRODUCT_NAME(NodeConfigFilterItem.LICENSE_PRODUCT_NAME.name()),
	LICENSE_VENDOR(NodeConfigFilterItem.LICENSE_VENDOR.name()),
	LICENSE_VENDOR_CONTACT(NodeConfigFilterItem.LICENSE_VENDOR_CONTACT.name()),
	LICENSE_SERIAL_NUMBER(NodeConfigFilterItem.LICENSE_SERIAL_NUMBER.name()),
	LICENSE_COUNT(NodeConfigFilterItem.LICENSE_COUNT.name()),
	LICENSE_EXPIRATION_DATE(NodeConfigFilterItem.LICENSE_EXPIRATION_DATE.name()),
	// @formatter:on

	/** ----- ユーザ任意情報 ----- */
	// @formatter:off
	CUSTOM_DISPLAY_NAME(NodeConfigFilterItem.CUSTOM_DISPLAY_NAME.name()),
	CUSTOM_VALUE(NodeConfigFilterItem.CUSTOM_VALUE.name());
	// @formatter:on

	private final String code;

	private NodeConfigFilterItemEnum(final String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

}
