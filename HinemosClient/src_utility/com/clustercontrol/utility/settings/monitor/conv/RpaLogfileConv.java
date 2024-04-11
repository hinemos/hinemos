/*
 * Copyright (c) 2022 NTT DATA INTELLILINK Corporation.
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
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.MonitorStringValueInfoResponse;
import org.openapitools.client.model.RpaLogFileCheckInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.monitor.xml.RpaLogfileInfo;
import com.clustercontrol.utility.settings.monitor.xml.RpaLogfileMonitor;
import com.clustercontrol.utility.settings.monitor.xml.RpaLogfileMonitors;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.settings.monitor.xml.StringValue;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.version.util.VersionUtil;

/**
 * RPAログファイル 監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 *
 */
public class RpaLogfileConv {
	private final static Log logger = LogFactory.getLog(RpaLogfileConv.class);

	/**
	 * 同一バイナリ化対応により、スキーマ情報はHinemosVersion.jarのVersionUtilクラスから取得されることになった。
	 * スキーマ情報の一覧はhinemos_version.properties.implに記載されている。
	 * スキーマ情報に変更がある場合は、まずbuild_common_version.properties.implを修正し、
	 * 対象のスキーマ情報が初回の修正であるならばhinemos_version.properties.implも修正する。
	 */
	private final static String SCHEMA_TYPE = VersionUtil.getSchemaProperty("MONITOR.RPALOGFILE.SCHEMATYPE");
	private final static String SCHEMA_VERSION = VersionUtil.getSchemaProperty("MONITOR.RPALOGFILE.SCHEMAVERSION");
	private final static String SCHEMA_REVISION =VersionUtil.getSchemaProperty("MONITOR.RPALOGFILE.SCHEMAREVISION");

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
	 * Castor で作成した形式の リソース 監視設定情報を DTO へ変換する<BR>
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
	public static List<MonitorInfoResponse> createMonitorInfoList(RpaLogfileMonitors logfileMonitors) throws ConvertorException, HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, InvalidSetting, ParseException {
		List<MonitorInfoResponse> monitorInfoList = new LinkedList<MonitorInfoResponse>();

		for (RpaLogfileMonitor logfileMonitor : logfileMonitors.getRpaLogfileMonitor()) {
			logger.debug("Monitor Id : " + logfileMonitor.getMonitor().getMonitorId());
			MonitorInfoResponse monitorInfo = MonitorConv.createMonitorInfo(logfileMonitor.getMonitor());

			StringValue[] values = logfileMonitor.getStringValue();
			MonitorConv.sort(values);
			for (StringValue stringValue : values) {
				monitorInfo.getStringValueInfo().add(MonitorConv.createMonitorStringValueInfo(stringValue));
			}

			monitorInfo.setRpaLogFileCheckInfo(createRpaLogfileCheckInfo(logfileMonitor.getRpaLogfileInfo()));
			monitorInfoList.add(monitorInfo);
		}

		return monitorInfoList;
	}

	/**
	 * DTO から、Castor で作成した形式の リソース 監視設定情報へ変換する<BR>
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
	public static RpaLogfileMonitors createRpaLogfileMonitors(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		RpaLogfileMonitors logfileMonitors = new RpaLogfileMonitors();

		for (MonitorInfoResponse monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo = MonitorsettingRestClientWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			RpaLogfileMonitor logfileMonitor = new RpaLogfileMonitor();
			logfileMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));
			int orderNo = 0;
			for (MonitorStringValueInfoResponse monitorStringValueInfo : monitorInfo.getStringValueInfo()) {
				logfileMonitor.addStringValue(MonitorConv.createStringValue(monitorInfo.getMonitorId(),monitorStringValueInfo, ++orderNo));
			}

			logfileMonitor.setRpaLogfileInfo(createRpaLogfileInfo(monitorInfo));
			logfileMonitors.addRpaLogfileMonitor(logfileMonitor);
		}

		logfileMonitors.setCommon(MonitorConv.versionDto2Xml());
		logfileMonitors.setSchemaInfo(getSchemaVersion());

		return logfileMonitors;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static RpaLogfileInfo createRpaLogfileInfo(MonitorInfoResponse monitorInfo) {
		RpaLogfileInfo logfileInfo = new RpaLogfileInfo();
		logfileInfo.setMonitorTypeId("");
		logfileInfo.setMonitorId(monitorInfo.getMonitorId());
		logfileInfo.setDirectory(monitorInfo.getRpaLogFileCheckInfo().getDirectory());
		logfileInfo.setFileName(monitorInfo.getRpaLogFileCheckInfo().getFileName());
		logfileInfo.setFileEncoding(monitorInfo.getRpaLogFileCheckInfo().getFileEncoding());
		logfileInfo.setRpaToolEnvId(monitorInfo.getRpaLogFileCheckInfo().getRpaToolEnvId());

		return logfileInfo;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static RpaLogFileCheckInfoResponse createRpaLogfileCheckInfo(RpaLogfileInfo rpaLogfileInfo) {
		RpaLogFileCheckInfoResponse rpaLogfileCheckInfo = new RpaLogFileCheckInfoResponse();
		rpaLogfileCheckInfo.setRpaToolEnvId(rpaLogfileInfo.getRpaToolEnvId());
		rpaLogfileCheckInfo.setDirectory(rpaLogfileInfo.getDirectory());
		rpaLogfileCheckInfo.setFileName(rpaLogfileInfo.getFileName());
		rpaLogfileCheckInfo.setFileEncoding(rpaLogfileInfo.getFileEncoding());

		return rpaLogfileCheckInfo;
	}
}
