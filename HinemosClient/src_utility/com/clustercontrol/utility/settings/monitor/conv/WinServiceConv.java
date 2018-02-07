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
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.settings.monitor.xml.TruthValue;
import com.clustercontrol.utility.settings.monitor.xml.WinServiceInfo;
import com.clustercontrol.utility.settings.monitor.xml.WinServiceMonitor;
import com.clustercontrol.utility.settings.monitor.xml.WinServiceMonitors;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;
import com.clustercontrol.ws.monitor.MonitorTruthValueInfo;
import com.clustercontrol.ws.monitor.WinServiceCheckInfo;

/**
 * Windows サービス 監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 *
 * @version 6.1.0
 * @since 2.0.0
 *
 */
public class WinServiceConv {
	private final static Log logger = LogFactory.getLog(WinServiceConv.class);

	static private String SCHEMA_TYPE = "H";
	static private String SCHEMA_VERSION = "1";
	static private String SCHEMA_REVISION = "2";

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

	/*スキーマのバージョンチェック*/
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
	 * <BR>
	 *
	 * @return
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static WinServiceMonitors createWinServiceMonitors(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		WinServiceMonitors winServiceMonitors = new WinServiceMonitors();

		for (MonitorInfo monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo = MonitorSettingEndpointWrapper
					.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			WinServiceMonitor winServiceMonitor = new WinServiceMonitor();
			winServiceMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));

			for (MonitorTruthValueInfo truthValueInfo : monitorInfo.getTruthValueInfo()) {
				winServiceMonitor.addTruthValue(MonitorConv.createTruthValue(truthValueInfo));
			}

			winServiceMonitor.setWinServiceInfo(createWinServiceInfo(monitorInfo.getWinServiceCheckInfo()));
			winServiceMonitors.addWinServiceMonitor(winServiceMonitor);
		}

		winServiceMonitors.setCommon(MonitorConv.versionDto2Xml());
		winServiceMonitors.setSchemaInfo(getSchemaVersion());

		return winServiceMonitors;
	}

	public static List<MonitorInfo> createMonitorInfoList(WinServiceMonitors winServiceMonitors) throws ConvertorException {
		List<MonitorInfo> monitorInfoList = new LinkedList<MonitorInfo>();

		for (WinServiceMonitor winServiceMonitor : winServiceMonitors.getWinServiceMonitor()) {
			logger.debug("Monitor Id : " + winServiceMonitor.getMonitor().getMonitorId());

			MonitorInfo monitorInfo = MonitorConv.createMonitorInfo(winServiceMonitor.getMonitor());
			for (TruthValue truthValue : winServiceMonitor.getTruthValue()) {
				monitorInfo.getTruthValueInfo().add(MonitorConv.createTruthValue(truthValue));
			}

			monitorInfo.setWinServiceCheckInfo(createWinServiceCheckInfo(winServiceMonitor.getWinServiceInfo()));
			monitorInfoList.add(monitorInfo);
		}

		return monitorInfoList;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static WinServiceInfo createWinServiceInfo(WinServiceCheckInfo winServiceCheckInfo) {
		WinServiceInfo winServiceInfo = new WinServiceInfo();
		winServiceInfo.setMonitorTypeId("");

		winServiceInfo.setMonitorId(winServiceCheckInfo.getMonitorId());
		winServiceInfo.setServiceName(winServiceCheckInfo.getServiceName());

		return winServiceInfo;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static WinServiceCheckInfo createWinServiceCheckInfo(WinServiceInfo winServiceInfo) {
		WinServiceCheckInfo winServiceCheckInfo = new WinServiceCheckInfo();
		winServiceCheckInfo.setMonitorTypeId("");

		winServiceCheckInfo.setMonitorId(winServiceInfo.getMonitorId());
		winServiceCheckInfo.setServiceName(winServiceInfo.getServiceName());

		return winServiceCheckInfo;
	}
}