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

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.monitor.xml.LogfileInfo;
import com.clustercontrol.utility.settings.monitor.xml.LogfileMonitor;
import com.clustercontrol.utility.settings.monitor.xml.LogfileMonitors;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.settings.monitor.xml.StringValue;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.LogfileCheckInfo;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;
import com.clustercontrol.ws.monitor.MonitorStringValueInfo;

/**
 * ログファイル 監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 *
 * @version 6.1.0
 * @since 2.0.0
 *
 */
public class LogfileConv {
	private final static Log logger = LogFactory.getLog(LogfileConv.class);

	private final static String SCHEMA_TYPE = "H";
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
	 * Castor で作成した形式の リソース 監視設定情報を DTO へ変換する<BR>
	 *
	 * @param
	 * @return
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static List<MonitorInfo> createMonitorInfoList(LogfileMonitors logfileMonitors) throws ConvertorException, HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		List<MonitorInfo> monitorInfoList = new LinkedList<MonitorInfo>();

		for (LogfileMonitor logfileMonitor : logfileMonitors.getLogfileMonitor()) {
			logger.debug("Monitor Id : " + logfileMonitor.getMonitor().getMonitorId());
			MonitorInfo monitorInfo = MonitorConv.createMonitorInfo(logfileMonitor.getMonitor());

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
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static LogfileMonitors createLogfileMonitors(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		LogfileMonitors logfileMonitors = new LogfileMonitors();

		for (MonitorInfo monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo = MonitorSettingEndpointWrapper
					.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			LogfileMonitor logfileMonitor = new LogfileMonitor();
			logfileMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));
			int orderNo = 0;
			for (MonitorStringValueInfo monitorStringValueInfo : monitorInfo.getStringValueInfo()) {
				logfileMonitor.addStringValue(MonitorConv.createStringValue(monitorStringValueInfo, ++orderNo));
			}

			logfileMonitor.setLogfileInfo(createLogfileInfo(monitorInfo.getLogfileCheckInfo()));
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
	private static LogfileInfo createLogfileInfo(LogfileCheckInfo logfileCheckInfo) {
		LogfileInfo logfileInfo = new LogfileInfo();
		logfileInfo.setMonitorTypeId("");
		logfileInfo.setMonitorId(logfileCheckInfo.getMonitorId());
		logfileInfo.setDirectory(logfileCheckInfo.getDirectory());
		logfileInfo.setFileName(logfileCheckInfo.getFileName());
		logfileInfo.setFileEncoding(logfileCheckInfo.getFileEncoding());
		logfileInfo.setFileReturnCode(logfileCheckInfo.getFileReturnCode());
		logfileInfo.setFilePatternHead(logfileCheckInfo.getPatternHead());
		logfileInfo.setFilePatternTail(logfileCheckInfo.getPatternTail());
		if (logfileCheckInfo.getMaxBytes() != null){
			logfileInfo.setFileMaxBytes(logfileCheckInfo.getMaxBytes());
		}

		return logfileInfo;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static LogfileCheckInfo createLogfileCheckInfo(LogfileInfo logfileInfo) {
		LogfileCheckInfo logfileCheckInfo = new LogfileCheckInfo();
		logfileCheckInfo.setMonitorTypeId("");
		logfileCheckInfo.setMonitorId(logfileInfo.getMonitorId());
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
