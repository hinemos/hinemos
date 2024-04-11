/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.conv;

import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openapitools.client.model.BinaryPatternInfoResponse;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.PacketCheckInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.monitor.xml.BinaryValue;
import com.clustercontrol.utility.settings.monitor.xml.PacketCapInfo;
import com.clustercontrol.utility.settings.monitor.xml.PacketCapMonitor;
import com.clustercontrol.utility.settings.monitor.xml.PacketCapMonitors;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.version.util.VersionUtil;

/**
 * パケットキャプチャ 監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 *
 * @version 6.1.0
 * @since 6.1.0
 *
 */
public class PacketCapConv {
	private final static Log logger = LogFactory.getLog(PacketCapConv.class);

	/**
	 * 同一バイナリ化対応により、スキーマ情報はHinemosVersion.jarのVersionUtilクラスから取得されることになった。
	 * スキーマ情報の一覧はhinemos_version.properties.implに記載されている。
	 * スキーマ情報に変更がある場合は、まずbuild_common_version.properties.implを修正し、
	 * 対象のスキーマ情報が初回の修正であるならばhinemos_version.properties.implも修正する。
	 */
	private final static String SCHEMA_TYPE = VersionUtil.getSchemaProperty("MONITOR.PACKETCAP.SCHEMATYPE");
	private final static String SCHEMA_VERSION = VersionUtil.getSchemaProperty("MONITOR.PACKETCAP.SCHEMAVERSION");
	private final static String SCHEMA_REVISION =VersionUtil.getSchemaProperty("MONITOR.PACKETCAP.SCHEMAREVISION");

	/**
	 * <BR>
	 *
	 * @return
	 */
	public static SchemaInfo getSchemaVersion(){
		SchemaInfo schema = new SchemaInfo();

		schema.setSchemaType(SCHEMA_TYPE);
		schema.setSchemaVersion(SCHEMA_VERSION);
		schema.setSchemaRevision(SCHEMA_REVISION);

		return schema;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	public static int checkSchemaVersion(SchemaInfo schemaInfo) {
		return BaseConv.checkSchemaVersion(
				SCHEMA_TYPE,
				SCHEMA_VERSION,
				SCHEMA_REVISION,
				schemaInfo.getSchemaType(),
				schemaInfo.getSchemaVersion(),
				schemaInfo.getSchemaRevision()
				);
	}

	/**
	 * Castor で作成した形式の パケットキャプチャ 監視設定情報を DTO へ変換する<BR>
	 *
	 * @param
	 * @return
	 * @throws ParseException 
	 * @throws InvalidSetting 
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static List<MonitorInfoResponse> createMonitorInfoList(PacketCapMonitors packetCapMonitors) throws ConvertorException, HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, InvalidSetting, ParseException {
		List<MonitorInfoResponse> monitorInfoList = new LinkedList<MonitorInfoResponse>();

		for (PacketCapMonitor packetCapMonitor : packetCapMonitors.getPacketCapMonitor()) {
			logger.debug("Monitor Id : " + packetCapMonitor.getMonitor().getMonitorId());
			MonitorInfoResponse monitorInfo = MonitorConv.createMonitorInfo(packetCapMonitor.getMonitor());

			BinaryValue[] binaryVals = packetCapMonitor.getBinaryValue();
			MonitorConv.sort(binaryVals);
			for (BinaryValue binaryValue : binaryVals) {
				monitorInfo.getBinaryPatternInfo().add(MonitorConv.createMonitorBinaryValueInfo(binaryValue));
			}

			monitorInfo.setPacketCheckInfo(createPacketCheckInfo(packetCapMonitor.getPacketCapInfo()));
			monitorInfoList.add(monitorInfo);
		}

		return monitorInfoList;
	}

	/**
	 * DTO から、Castor で作成した形式の パケットキャプチャ 監視設定情報へ変換する<BR>
	 *
	 * @param
	 * @return
	 * @throws RestConnectFailed 
	 * @throws ParseException 
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static PacketCapMonitors createPacketCapMonitors(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		PacketCapMonitors packetCapMonitors = new PacketCapMonitors();

		for (MonitorInfoResponse monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo = MonitorsettingRestClientWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			PacketCapMonitor packetCapMonitor = new PacketCapMonitor();
			packetCapMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));
			
			int orderNo = 0;
			for (BinaryPatternInfoResponse binaryPatternInfo : monitorInfo.getBinaryPatternInfo()) {
				packetCapMonitor.addBinaryValue(MonitorConv.createBinaryValue(monitorInfo.getMonitorId(),binaryPatternInfo, ++orderNo));
			}

			packetCapMonitor.setPacketCapInfo(createPacketInfo(monitorInfo));
			packetCapMonitors.addPacketCapMonitor(packetCapMonitor);
		}

		packetCapMonitors.setCommon(MonitorConv.versionDto2Xml());
		packetCapMonitors.setSchemaInfo(getSchemaVersion());

		return packetCapMonitors;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static PacketCapInfo createPacketInfo(MonitorInfoResponse monitorInfo) {
		PacketCapInfo packetCapInfo = new PacketCapInfo();
		packetCapInfo.setMonitorTypeId("");
		packetCapInfo.setMonitorId(monitorInfo.getMonitorId());

		packetCapInfo.setFilterStr(monitorInfo.getPacketCheckInfo().getFilterStr());
		packetCapInfo.setPromiscuousMode(monitorInfo.getPacketCheckInfo().getPromiscuousMode());

		return packetCapInfo;
	}
	
	
	private static PacketCheckInfoResponse createPacketCheckInfo(PacketCapInfo packetCapInfo) {
		PacketCheckInfoResponse packetCapCheckInfo = new PacketCheckInfoResponse();

		packetCapCheckInfo.setFilterStr(packetCapInfo.getFilterStr());
		packetCapCheckInfo.setPromiscuousMode(packetCapInfo.getPromiscuousMode());

		return packetCapCheckInfo;
	}

}
