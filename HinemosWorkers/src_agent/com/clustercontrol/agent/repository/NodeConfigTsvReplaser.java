/*
 * Copyright (c) 2019 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.agent.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.bean.IpAddressInfo;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.util.NetworkInterfaceUtil;
import com.clustercontrol.ws.agentnodeconfig.NodeNetworkInterfaceInfo;
import com.clustercontrol.ws.repository.NodeCpuInfo;
import com.clustercontrol.ws.repository.NodeDiskInfo;
import com.clustercontrol.ws.repository.NodeFilesystemInfo;
import com.clustercontrol.ws.repository.NodeHostnameInfo;
import com.clustercontrol.ws.repository.NodeMemoryInfo;
import com.clustercontrol.ws.repository.NodeNetstatInfo;
import com.clustercontrol.ws.repository.NodeOsInfo;
import com.clustercontrol.ws.repository.NodePackageInfo;
import com.clustercontrol.ws.repository.NodeProcessInfo;

public class NodeConfigTsvReplaser {

	// Windowsフラグ
	private boolean winFlg = false;

	public NodeConfigTsvReplaser(boolean winFlg) {
		this.winFlg = winFlg;
	}

	private static Log log = LogFactory.getLog(NodeConfigTsvReplaser.class);
	private static final String DELIMITER = "() : ";

	/**
	 * プロセス情報を設定する
	 * 
	 * @param token
	 * @return CMDBRecordProcess
	 * @throws HinemosUnknown
	 */
	protected NodeProcessInfo setRecordProcess(String[] token) throws HinemosUnknown {

		// ■プロセス
		NodeProcessInfo record_process = new NodeProcessInfo();

		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		try {
			// プロセス名
			record_process.setProcessName(getParam(NodeConfigConstant.PROCESS_NAME, token));
			// 引数付フルパス
			record_process.setPath(getParam(NodeConfigConstant.PROCESS_PATH, token));
			// 実行ユーザ
			record_process.setExecUser(getParam(NodeConfigConstant.PROCESS_USER, token));
			// PID
			String pidStr = getParam(NodeConfigConstant.PROCESS_ID, token);
			if (!"".equals(pidStr)) {
				record_process.setPid(Integer.valueOf(pidStr));
			}
			// 起動日時
			String StartUpDateStr = getParam(NodeConfigConstant.PROCESS_UPTIME, token);
			if (!"".equals(StartUpDateStr)) {
				record_process.setStartupDateTime(Long.parseLong(StartUpDateStr) * 1000);
			}

			return record_process;
		} catch (HinemosUnknown e) {
			// 共通部品でログ出力済なのでここでは出力しない.
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (Exception e) {
			log.warn(methodName + DELIMITER + "failed to replace from TSV to JavaDTO.", e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/**
	 * パッケージ情報を設定する
	 * 
	 * @param token
	 * @return CMDBRecordPackage
	 * @throws HinemosUnknown
	 */
	protected NodePackageInfo setRecordPackage(String[] token) throws HinemosUnknown {
		// ■パッケージ
		NodePackageInfo record_pkg = new NodePackageInfo();

		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		try {
			// パッケージID
			if (this.winFlg) {
				record_pkg.setPackageId(getParam(NodeConfigConstant.PACKAGE_ID, token));
			} else {
				record_pkg.setPackageId(String.format("%s.%s", getParam(NodeConfigConstant.PACKAGE_NAME, token),
						getParam(NodeConfigConstant.PACKAGE_ARCHITECTURE, token)));
			}

			// パッケージ名
			record_pkg.setPackageName(getParam(NodeConfigConstant.PACKAGE_NAME, token));
			// リリース
			record_pkg.setRelease(getParam(NodeConfigConstant.PACKAGE_RELEASE, token));
			// バージョン
			record_pkg.setVersion(getParam(NodeConfigConstant.PACKAGE_VERSION, token));

			// インストール日時
			String instDateStr = getParam(NodeConfigConstant.PACKAGE_INSTALL_DATE, token);
			if (!"".equals(instDateStr)) {
				record_pkg.setInstallDate(Long.parseLong(instDateStr) * 1000);
			}

			// ベンダ
			record_pkg.setVendor(getParam(NodeConfigConstant.PACKAGE_VENDOR, token));
			// アーキテクチャ
			record_pkg.setArchitecture(getParam(NodeConfigConstant.PACKAGE_ARCHITECTURE, token));

			return record_pkg;
		} catch (HinemosUnknown e) {
			// 共通部品でログ出力済なのでここでは出力しない.
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (Exception e) {
			log.warn(methodName + DELIMITER + "failed to replace from TSV to JavaDTO.", e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/**
	 * HW NIC情報を設定する
	 * 
	 * @param token
	 * @return
	 * @throws HinemosUnknown
	 */
	protected NodeNetworkInterfaceInfo setRecordNic(String[] token) throws HinemosUnknown {
		// NIC
		NodeNetworkInterfaceInfo record_nic = new NodeNetworkInterfaceInfo();

		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		try {
			// 表示名
			record_nic.setDeviceDisplayName(getParam(NodeConfigConstant.NIC_NAME, token));
			// デバイス名
			record_nic.setDeviceName(getParam(NodeConfigConstant.NIC_DEVICE_NAME, token));
			// デバイスINDEX
			String deviceIdxStr = getParam(NodeConfigConstant.NIC_DEVICE_INDEX, token);
			if (!"".equals(deviceIdxStr)) {
				record_nic.setDeviceIndex(Integer.valueOf(deviceIdxStr));
			}
			// デバイス種別
			String deviceTypeStr = getParam(NodeConfigConstant.NIC_DEVICE_TYPE, token);
			if (!"".equals(deviceTypeStr)) {
				record_nic.setDeviceType(deviceTypeStr);
			} else {
				record_nic.setDeviceType(NodeConfigConstant.DEFAULT_NIC_TYPE);
			}
			// デバイスサイズ
			String deviceSizeStr = getParam(NodeConfigConstant.NIC_DEVICE_SIZE, token);
			if (!"".equals(deviceSizeStr)) {
				record_nic.setDeviceSize(Integer.valueOf(deviceSizeStr));
			}
			// デバイスサイズ単位
			record_nic.setDeviceSizeUnit(getParam(NodeConfigConstant.NIC_DEVICE_SIZE_UNIT, token));
			// 説明
			record_nic.setDeviceDescription(getParam(NodeConfigConstant.NIC_DESCRIPTION, token));
			// IPアドレスv4
			String ipv4Address = getParam(NodeConfigConstant.NIC_IP_ADDRESS, token);
			IpAddressInfo ipv4Info;
			if(ipv4Address.equals("")){
				record_nic.setNicIpAddress("");
			}else{
				try {
					ipv4Info = NetworkInterfaceUtil.getIpAddressInfo(ipv4Address);
				} catch (InvalidSetting | HinemosUnknown e) {
					ipv4Info = null;
					throw new HinemosUnknown(e.getMessage(), e);
				}
				if (ipv4Info != null) {
					record_nic.setNicIpAddress(ipv4Info.getOnlyIpAddress());
				}
			}
			// MACアドレス
			record_nic.setNicMacAddress(getParam(NodeConfigConstant.NIC_MAC_ADDRESS, token));

			return record_nic;
		} catch (HinemosUnknown e) {
			// 共通部品でログ出力済なのでここでは出力しない.
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (Exception e) {
			log.warn(methodName + DELIMITER + "failed to replace from TSV to JavaDTO.", e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

	}

	/**
	 * HW CPU情報を設定する
	 * 
	 * @param token
	 * @return
	 * @throws HinemosUnknown
	 */
	protected NodeCpuInfo setRecordCpu(String[] token) throws HinemosUnknown {
		// CPU
		NodeCpuInfo recordCpu = new NodeCpuInfo();

		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		try {
			// 表示名
			recordCpu.setDeviceDisplayName(getParam(NodeConfigConstant.CPU_NAME, token));
			// デバイス名
			recordCpu.setDeviceName(getParam(NodeConfigConstant.CPU_DEVICE_NAME, token));
			// デバイスINDEX
			String deviceIdxStr = getParam(NodeConfigConstant.CPU_DEVICE_INDEX, token);
			if (!"".equals(deviceIdxStr)) {
				recordCpu.setDeviceIndex(Integer.valueOf(deviceIdxStr));
			}
			// デバイス種別
			recordCpu.setDeviceType(getParam(NodeConfigConstant.CPU_DEVICE_TYPE, token));
			// デバイスサイズ
			String tmpIntStr = getParam(NodeConfigConstant.CPU_DEVICE_SIZE, token);
			if (!"".equals(tmpIntStr)) {
				recordCpu.setDeviceSize(Integer.valueOf(tmpIntStr));
			}
			// デバイスサイズ単位
			recordCpu.setDeviceSizeUnit(getParam(NodeConfigConstant.CPU_DEVICE_SIZE_UNIT, token));
			// 説明
			recordCpu.setDeviceDescription(getParam(NodeConfigConstant.CPU_DESCRIPTION, token));
			// コア数
			tmpIntStr = getParam(NodeConfigConstant.CPU_CORE_COUNT, token);
			if (!"".equals(tmpIntStr)) {
				recordCpu.setCoreCount(Integer.valueOf(tmpIntStr));
			}
			// スレッド数
			tmpIntStr = getParam(NodeConfigConstant.CPU_THREAD_COUNT, token);
			if (!"".equals(tmpIntStr)) {
				recordCpu.setThreadCount(Integer.valueOf(tmpIntStr));
			}
			// クロック数
			tmpIntStr = getParam(NodeConfigConstant.CPU_CLOCK_COUNT, token);
			if (!"".equals(tmpIntStr)) {
				recordCpu.setClockCount(Integer.valueOf(tmpIntStr));
			}
			return recordCpu;
		} catch (HinemosUnknown e) {
			// 共通部品でログ出力済なのでここでは出力しない.
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (Exception e) {
			log.warn(methodName + DELIMITER + "failed to replace from TSV to JavaDTO.", e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

	}

	/**
	 * HW DISK情報を設定する
	 * 
	 * @param token
	 * @return
	 * @throws HinemosUnknown
	 */
	protected NodeDiskInfo setRecordDisk(String[] token) throws HinemosUnknown {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		NodeDiskInfo record = new NodeDiskInfo();
		try {
			// 表示名
			record.setDeviceDisplayName(getParam(NodeConfigConstant.DISK_NAME, token));
			// デバイス名
			record.setDeviceName(getParam(NodeConfigConstant.DISK_DEVICE_NAME, token));
			// デバイスINDEX
			String deviceIdxStr = getParam(NodeConfigConstant.DISK_DEVICE_INDEX, token);
			if (!"".equals(deviceIdxStr)) {
				record.setDeviceIndex(Integer.valueOf(deviceIdxStr));
			}
			// デバイス種別
			record.setDeviceType(getParam(NodeConfigConstant.DISK_DEVICE_TYPE, token));
			// デバイスサイズ
			String tmpIntStr = getParam(NodeConfigConstant.DISK_DEVICE_SIZE, token);
			if (!"".equals(tmpIntStr)) {
				record.setDeviceSize(Integer.valueOf(tmpIntStr));
			}
			// デバイスサイズ単位
			record.setDeviceSizeUnit(getParam(NodeConfigConstant.DISK_DEVICE_SIZE_UNIT, token));
			// 説明
			record.setDeviceDescription(getParam(NodeConfigConstant.DISK_DESCRIPTION, token));
			return record;
		} catch (HinemosUnknown e) {
			// 共通部品でログ出力済なのでここでは出力しない.
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (Exception e) {
			log.warn(methodName + DELIMITER + "failed to replace from TSV to JavaDTO.", e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

	}

	/**
	 * HW ファイルシステム情報を設定する
	 * 
	 * @param token
	 * @return
	 * @throws HinemosUnknown
	 */
	protected NodeFilesystemInfo setRecordFsystem(String[] token) throws HinemosUnknown {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		NodeFilesystemInfo record = new NodeFilesystemInfo();
		try {
			// 表示名
			record.setDeviceDisplayName(getParam(NodeConfigConstant.FSYSTEM_NAME, token));
			// デバイス名
			record.setDeviceName(getParam(NodeConfigConstant.FSYSTEM_DEVICE_NAME, token));
			// デバイスINDEX
			String deviceIdxStr = getParam(NodeConfigConstant.FSYSTEM_DEVICE_INDEX, token);
			if (!"".equals(deviceIdxStr)) {
				record.setDeviceIndex(Integer.valueOf(deviceIdxStr));
			}
			// デバイス種別
			record.setDeviceType(getParam(NodeConfigConstant.FSYSTEM_DEVICE_TYPE, token));
			// デバイスサイズ
			String tmpIntStr = getParam(NodeConfigConstant.FSYSTEM_DEVICE_SIZE, token);
			if (!"".equals(tmpIntStr)) {
				record.setDeviceSize(Integer.valueOf(tmpIntStr));
			}
			// デバイスサイズ単位
			record.setDeviceSizeUnit(getParam(NodeConfigConstant.FSYSTEM_DEVICE_SIZE_UNIT, token));
			// 説明
			record.setDeviceDescription(getParam(NodeConfigConstant.FSYSTEM_DESCRIPTION, token));
			// ファイルシステム種別
			record.setFilesystemType(getParam(NodeConfigConstant.FSYSTEM_TYPE, token));
			return record;
		} catch (HinemosUnknown e) {
			// 共通部品でログ出力済なのでここでは出力しない.
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (Exception e) {
			log.warn(methodName + DELIMITER + "failed to replace from TSV to JavaDTO.", e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

	}

	/**
	 * ネットワーク接続情報を設定する
	 * 
	 * @param token
	 * @return
	 * @throws HinemosUnknown
	 */
	protected NodeNetstatInfo setRecordNetstat(String[] token) throws HinemosUnknown {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		NodeNetstatInfo record = new NodeNetstatInfo();
		try {
			// プロトコル.
			String protocol = getParam(NodeConfigConstant.NETSTAT_PROTOCOL, token);
			record.setProtocol(protocol);
			// ローカルIPアドレス.
			record.setLocalIpAddress(getParam(NodeConfigConstant.NETSTAT_LOCAL_IP_ADDRESS, token));
			// ローカルポート.
			record.setLocalPort(getParam(NodeConfigConstant.NETSTAT_LOCAL_PORT, token));
			// 外部IPアドレス.
			record.setForeignIpAddress(getParam(NodeConfigConstant.NETSTAT_FOREIGN_IP_ADDRESS, token));
			// 外部ポート.
			record.setForeignPort(getParam(NodeConfigConstant.NETSTAT_FOREIGN_PORT, token));
			// プロセス名.
			record.setProcessName(getParam(NodeConfigConstant.NETSTAT_PROCESS_NAME, token));
			// プロセスID.
			String tmpIntStr = getParam(NodeConfigConstant.NETSTAT_PROCESS_ID, token);
			if (!"".equals(tmpIntStr)) {
				record.setPid(Integer.valueOf(tmpIntStr));
			}
			// 状態.
			record.setStatus(getParam(NodeConfigConstant.NETSTAT_STATUS, token));

			return record;
		} catch (HinemosUnknown e) {
			// 共通部品でログ出力済なのでここでは出力しない.
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (Exception e) {
			log.warn(methodName + DELIMITER + "failed to replace from TSV to JavaDTO.", e);
			throw new HinemosUnknown(e.getMessage(), e);
		}

	}

	/**
	 * OS情報を設定する
	 * 
	 * @param token
	 * @return
	 * @throws HinemosUnknown
	 */
	protected NodeOsInfo setRecordOs(String[] token) throws HinemosUnknown {
		NodeOsInfo returnInfo = new NodeOsInfo();

		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		try {
			// ＯＳ名
			returnInfo.setOsName(getParam(NodeConfigConstant.OS_NAME, token));
			// ＯＳリリース
			returnInfo.setOsRelease(getParam(NodeConfigConstant.OS_RELEASE, token));
			// ＯＳバージョン
			returnInfo.setOsVersion(getParam(NodeConfigConstant.OS_VERSION, token));
			// 起動日時
			String startDateStr = getParam(NodeConfigConstant.STARTUP_DATE_TIME, token);
			if (!"".equals(startDateStr)) {
				returnInfo.setStartupDateTime(Long.parseLong(startDateStr) * 1000);
			}
			return returnInfo;
		} catch (HinemosUnknown e) {
			// 共通部品でログ出力済なのでここでは出力しない.
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (Exception e) {
			log.warn(methodName + DELIMITER + "failed to replace from TSV to JavaDTO.", e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/**
	 * ホスト情報を設定する
	 * 
	 * @param token
	 * @return
	 * @throws HinemosUnknown
	 */
	protected NodeHostnameInfo setRecordHost(String[] token) throws HinemosUnknown {
		NodeHostnameInfo returnInfo = new NodeHostnameInfo();

		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();
		try {
			// ホスト名
			returnInfo.setHostname(getParam(NodeConfigConstant.HOST_NAME, token));
			return returnInfo;
		} catch (HinemosUnknown e) {
			// 共通部品でログ出力済なのでここでは出力しない.
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (Exception e) {
			log.warn(methodName + DELIMITER + "failed to replace from TSV to JavaDTO.", e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/**
	 * メモリ情報を設定する
	 * 
	 * @param token
	 * @return
	 * @throws HinemosUnknown
	 */
	protected NodeMemoryInfo setRecordMemory(String[] token) throws HinemosUnknown {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		NodeMemoryInfo returnInfo = new NodeMemoryInfo();
		try {
			// 表示名
			returnInfo.setDeviceDisplayName(getParam(NodeConfigConstant.MEMORY_NAME, token));
			// デバイス名
			returnInfo.setDeviceName(getParam(NodeConfigConstant.MEMORY_DEVICE_NAME, token));
			// デバイスINDEX
			String deviceIdxStr = getParam(NodeConfigConstant.MEMORY_DEVICE_INDEX, token);
			if (!"".equals(deviceIdxStr)) {
				returnInfo.setDeviceIndex(Integer.valueOf(deviceIdxStr));
			}
			// デバイス種別
			returnInfo.setDeviceType(getParam(NodeConfigConstant.MEMORY_DEVICE_TYPE, token));
			// デバイスサイズ
			String tmpIntStr = getParam(NodeConfigConstant.MEMORY_DEVICE_SIZE, token);
			if (!"".equals(tmpIntStr)) {
				returnInfo.setDeviceSize(Integer.valueOf(tmpIntStr));
			}
			// デバイスサイズ単位
			returnInfo.setDeviceSizeUnit(getParam(NodeConfigConstant.MEMORY_DEVICE_SIZE_UNIT, token));
			return returnInfo;
		} catch (HinemosUnknown e) {
			// 共通部品でログ出力済なのでここでは出力しない.
			throw new HinemosUnknown(e.getMessage(), e);
		} catch (Exception e) {
			log.warn(methodName + DELIMITER + "failed to replace from TSV to JavaDTO.", e);
			throw new HinemosUnknown(e.getMessage(), e);
		}
	}

	/***
	 * 配列から対象インデックスの値を取得する。範囲外のカラムを指定された場合はExceptionをthrow(スクリプト不具合の可能性あり)。
	 * 末尾のカラムについては、空文字の場合もエラーとなってしまうので、空文字返却。
	 * 
	 * @param idx
	 * @param params
	 * @return String
	 * @throws HinemosUnknown
	 */
	protected static String getParam(int idx, String[] params) throws HinemosUnknown {
		String methodName = Thread.currentThread().getStackTrace()[1].getMethodName();

		if (idx == params.length) {
			String header = params[0];
			String headerNext = params[1];
			String message = "replaced empty from TSV to JavaDTO, because columns output to TSV are insufficient." //
					+ "header=[" + header + "], " //
					+ "header next=[" + headerNext + "], " //
					+ "last column=[" + params[idx - 1] + "], " //
					+ "index to get=[" + idx + "], " //
					+ "length of columns=[" + params.length + "]";
			if (NodeConfigConstant.LINE_HEADER_NETSTAT.equals(header) && "udp".equals(headerNext.toLowerCase())) {
				log.debug(methodName + DELIMITER + message);
			} else {
				log.warn(methodName + DELIMITER + message);
			}
			return "";
		}

		if (idx < params.length) {
			return params[idx];
		}

		String message = "failed to replace from TSV to JavaDTO, because columns output to TSV are insufficient." //
				+ "header=[" + params[0] + "], " //
				+ "header next=[" + params[1] + "], " //
				+ "index to get=[" + idx + "], " //
				+ "length of columns=[" + params.length + "]";
		log.warn(methodName + DELIMITER + message);
		throw new HinemosUnknown(message);

	}
}
