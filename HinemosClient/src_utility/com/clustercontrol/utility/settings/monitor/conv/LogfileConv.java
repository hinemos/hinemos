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
import org.openapitools.client.model.LogfileCheckInfoResponse;
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.MonitorStringValueInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.monitor.xml.LogfileInfo;
import com.clustercontrol.utility.settings.monitor.xml.LogfileMonitor;
import com.clustercontrol.utility.settings.monitor.xml.LogfileMonitors;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.settings.monitor.xml.StringValue;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.version.util.VersionUtil;

/**
 * ログファイル 監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 *
 * @version 6.1.0
 * @since 2.0.0
 *
 */
public class LogfileConv {
	private final static Log logger = LogFactory.getLog(LogfileConv.class);

	/**
	 * 同一バイナリ化対応により、スキーマ情報はHinemosVersion.jarのVersionUtilクラスから取得されることになった。
	 * スキーマ情報の一覧はhinemos_version.properties.implに記載されている。
	 * スキーマ情報に変更がある場合は、まずbuild_common_version.properties.implを修正し、
	 * 対象のスキーマ情報が初回の修正であるならばhinemos_version.properties.implも修正する。
	 */
	private final static String SCHEMA_TYPE = VersionUtil.getSchemaProperty("MONITOR.LOGFILE.SCHEMATYPE");
	private final static String SCHEMA_VERSION = VersionUtil.getSchemaProperty("MONITOR.LOGFILE.SCHEMAVERSION");
	private final static String SCHEMA_REVISION =VersionUtil.getSchemaProperty("MONITOR.LOGFILE.SCHEMAREVISION");

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
	public static List<MonitorInfoResponse> createMonitorInfoList(LogfileMonitors logfileMonitors) throws ConvertorException, HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, InvalidSetting, ParseException {
		List<MonitorInfoResponse> monitorInfoList = new LinkedList<MonitorInfoResponse>();

		for (LogfileMonitor logfileMonitor : logfileMonitors.getLogfileMonitor()) {
			logger.debug("Monitor Id : " + logfileMonitor.getMonitor().getMonitorId());
			MonitorInfoResponse monitorInfo = MonitorConv.createMonitorInfo(logfileMonitor.getMonitor());

			StringValue[] values = logfileMonitor.getStringValue();
			MonitorConv.sort(values);
			for (StringValue stringValue : values) {
				monitorInfo.getStringValueInfo().add(MonitorConv.createMonitorStringValueInfo(stringValue));
			}

			monitorInfo.setLogfileCheckInfo(createLogfileCheckInfo(logfileMonitor.getLogfileInfo()));
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
	public static LogfileMonitors createLogfileMonitors(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		LogfileMonitors logfileMonitors = new LogfileMonitors();

		for (MonitorInfoResponse monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo = MonitorsettingRestClientWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			LogfileMonitor logfileMonitor = new LogfileMonitor();
			logfileMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));
			int orderNo = 0;
			for (MonitorStringValueInfoResponse monitorStringValueInfo : monitorInfo.getStringValueInfo()) {
				logfileMonitor.addStringValue(MonitorConv.createStringValue(monitorInfo.getMonitorId(),monitorStringValueInfo, ++orderNo));
			}

			logfileMonitor.setLogfileInfo(createLogfileInfo(monitorInfo));
			logfileMonitors.addLogfileMonitor(logfileMonitor);
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
	private static LogfileInfo createLogfileInfo(MonitorInfoResponse monitorInfo) {
		LogfileInfo logfileInfo = new LogfileInfo();
		logfileInfo.setMonitorTypeId("");
		logfileInfo.setMonitorId(monitorInfo.getMonitorId());
		logfileInfo.setDirectory(monitorInfo.getLogfileCheckInfo().getDirectory());
		logfileInfo.setFileName(monitorInfo.getLogfileCheckInfo().getFileName());
		logfileInfo.setFileEncoding(monitorInfo.getLogfileCheckInfo().getFileEncoding());
		logfileInfo.setFileReturnCode(monitorInfo.getLogfileCheckInfo().getFileReturnCode());
		logfileInfo.setFilePatternHead(monitorInfo.getLogfileCheckInfo().getPatternHead());
		logfileInfo.setFilePatternTail(monitorInfo.getLogfileCheckInfo().getPatternTail());
		if (monitorInfo.getLogfileCheckInfo().getMaxBytes() != null){
			logfileInfo.setFileMaxBytes(monitorInfo.getLogfileCheckInfo().getMaxBytes());
		}

		return logfileInfo;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static LogfileCheckInfoResponse createLogfileCheckInfo(LogfileInfo logfileInfo) {
		LogfileCheckInfoResponse logfileCheckInfo = new LogfileCheckInfoResponse();
		logfileCheckInfo.setDirectory(logfileInfo.getDirectory());
		logfileCheckInfo.setFileName(logfileInfo.getFileName());
		logfileCheckInfo.setFileEncoding(logfileInfo.getFileEncoding());
		logfileCheckInfo.setFileReturnCode(logfileInfo.getFileReturnCode());
		logfileCheckInfo.setPatternHead(logfileInfo.getFilePatternHead());
		logfileCheckInfo.setPatternTail(logfileInfo.getFilePatternTail());
		if(logfileInfo.getFileMaxBytes() > 0) {
			logfileCheckInfo.setMaxBytes(logfileInfo.getFileMaxBytes());
		}

		return logfileCheckInfo;
	}
}
