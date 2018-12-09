/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.conv;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.monitor.xml.BinaryValue;
import com.clustercontrol.utility.settings.monitor.xml.PacketCapInfo;
import com.clustercontrol.utility.settings.monitor.xml.PacketCapMonitor;
import com.clustercontrol.utility.settings.monitor.xml.PacketCapMonitors;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.monitor.BinaryPatternInfo;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;
import com.clustercontrol.ws.monitor.PacketCheckInfo;

/**
 * パケットキャプチャ 監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 *
 * @version 6.1.0
 * @since 6.1.0
 *
 */
public class PacketCapConv {
	private final static Log logger = LogFactory.getLog(PacketCapConv.class);

	private final static String SCHEMA_TYPE = "I";
	private final static String SCHEMA_VERSION = "1";
	private final static String SCHEMA_REVISION = "1";

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
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static List<MonitorInfo> createMonitorInfoList(PacketCapMonitors packetCapMonitors) throws ConvertorException, HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		List<MonitorInfo> monitorInfoList = new LinkedList<MonitorInfo>();

		for (PacketCapMonitor packetCapMonitor : packetCapMonitors.getPacketCapMonitor()) {
			logger.debug("Monitor Id : " + packetCapMonitor.getMonitor().getMonitorId());
			MonitorInfo monitorInfo = MonitorConv.createMonitorInfo(packetCapMonitor.getMonitor());

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
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static PacketCapMonitors createPacketCapMonitors(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		PacketCapMonitors packetCapMonitors = new PacketCapMonitors();

		for (MonitorInfo monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo = MonitorSettingEndpointWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			PacketCapMonitor packetCapMonitor = new PacketCapMonitor();
			packetCapMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));
			
			int orderNo = 0;
			for (BinaryPatternInfo binaryPatternInfo : monitorInfo.getBinaryPatternInfo()) {
				packetCapMonitor.addBinaryValue(MonitorConv.createBinaryValue(binaryPatternInfo, ++orderNo));
			}

			packetCapMonitor.setPacketCapInfo(createPacketInfo(monitorInfo.getPacketCheckInfo()));
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
	private static PacketCapInfo createPacketInfo(PacketCheckInfo packetCapCheckInfo) {
		PacketCapInfo packetCapInfo = new PacketCapInfo();
		packetCapInfo.setMonitorTypeId("");
		packetCapInfo.setMonitorId(packetCapCheckInfo.getMonitorId());

		packetCapInfo.setFilterStr(packetCapCheckInfo.getFilterStr());
		packetCapInfo.setPromiscuousMode(packetCapCheckInfo.isPromiscuousMode());

		return packetCapInfo;
	}
	
	
	private static PacketCheckInfo createPacketCheckInfo(PacketCapInfo packetCapInfo) {
		PacketCheckInfo packetCapCheckInfo = new PacketCheckInfo();

		packetCapCheckInfo.setFilterStr(packetCapInfo.getFilterStr());
		packetCapCheckInfo.setPromiscuousMode(packetCapInfo.getPromiscuousMode());

		return packetCapCheckInfo;
	}

}
