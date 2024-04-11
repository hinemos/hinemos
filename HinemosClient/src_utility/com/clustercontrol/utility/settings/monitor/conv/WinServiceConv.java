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
import org.openapitools.client.model.MonitorInfoResponse;
import org.openapitools.client.model.MonitorTruthValueInfoResponse;
import org.openapitools.client.model.WinServiceCheckInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.settings.monitor.xml.TruthValue;
import com.clustercontrol.utility.settings.monitor.xml.WinServiceInfo;
import com.clustercontrol.utility.settings.monitor.xml.WinServiceMonitor;
import com.clustercontrol.utility.settings.monitor.xml.WinServiceMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.version.util.VersionUtil;

/**
 * Windows サービス 監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 *
 * @version 6.1.0
 * @since 2.0.0
 *
 */
public class WinServiceConv {
	private final static Log logger = LogFactory.getLog(WinServiceConv.class);

	/**
	 * 同一バイナリ化対応により、スキーマ情報はHinemosVersion.jarのVersionUtilクラスから取得されることになった。
	 * スキーマ情報の一覧はhinemos_version.properties.implに記載されている。
	 * スキーマ情報に変更がある場合は、まずbuild_common_version.properties.implを修正し、
	 * 対象のスキーマ情報が初回の修正であるならばhinemos_version.properties.implも修正する。
	 */
	static private String SCHEMA_TYPE = VersionUtil.getSchemaProperty("MONITOR.WINSERVICE.SCHEMATYPE");
	static private String SCHEMA_VERSION = VersionUtil.getSchemaProperty("MONITOR.WINSERVICE.SCHEMAVERSION");
	static private String SCHEMA_REVISION =VersionUtil.getSchemaProperty("MONITOR.WINSERVICE.SCHEMAREVISION");

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
	 * @throws RestConnectFailed 
	 * @throws ParseException 
	 * @throws MonitorNotFound_Exception
	 * @throws InvalidUserPass_Exception
	 * @throws InvalidRole_Exception
	 * @throws HinemosUnknown_Exception
	 */
	public static WinServiceMonitors createWinServiceMonitors(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		WinServiceMonitors winServiceMonitors = new WinServiceMonitors();

		for (MonitorInfoResponse monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo = MonitorsettingRestClientWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			WinServiceMonitor winServiceMonitor = new WinServiceMonitor();
			winServiceMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));

			for (MonitorTruthValueInfoResponse truthValueInfo : monitorInfo.getTruthValueInfo()) {
				winServiceMonitor.addTruthValue(MonitorConv.createTruthValue(monitorInfo.getMonitorId(),truthValueInfo));
			}

			winServiceMonitor.setWinServiceInfo(createWinServiceInfo(monitorInfo));
			winServiceMonitors.addWinServiceMonitor(winServiceMonitor);
		}

		winServiceMonitors.setCommon(MonitorConv.versionDto2Xml());
		winServiceMonitors.setSchemaInfo(getSchemaVersion());

		return winServiceMonitors;
	}

	public static List<MonitorInfoResponse> createMonitorInfoList(WinServiceMonitors winServiceMonitors) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		List<MonitorInfoResponse> monitorInfoList = new LinkedList<MonitorInfoResponse>();

		for (WinServiceMonitor winServiceMonitor : winServiceMonitors.getWinServiceMonitor()) {
			logger.debug("Monitor Id : " + winServiceMonitor.getMonitor().getMonitorId());

			MonitorInfoResponse monitorInfo = MonitorConv.createMonitorInfo(winServiceMonitor.getMonitor());
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
	private static WinServiceInfo createWinServiceInfo(MonitorInfoResponse  monitorInfo) {
		WinServiceInfo winServiceInfo = new WinServiceInfo();
		winServiceInfo.setMonitorTypeId("");

		winServiceInfo.setMonitorId(monitorInfo.getMonitorId());
		winServiceInfo.setServiceName(monitorInfo.getWinServiceCheckInfo().getServiceName());

		return winServiceInfo;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static WinServiceCheckInfoResponse  createWinServiceCheckInfo(WinServiceInfo winServiceInfo) {
		WinServiceCheckInfoResponse  winServiceCheckInfo = new WinServiceCheckInfoResponse ();

		winServiceCheckInfo.setServiceName(winServiceInfo.getServiceName());

		return winServiceCheckInfo;
	}
}