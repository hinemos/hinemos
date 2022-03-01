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
import org.openapitools.client.model.MonitorTruthValueInfoResponse;
import org.openapitools.client.model.RpaManagementToolServiceCheckInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.monitor.xml.RpaManagementToolServiceInfo;
import com.clustercontrol.utility.settings.monitor.xml.RpaManagementToolServiceMonitor;
import com.clustercontrol.utility.settings.monitor.xml.RpaManagementToolServiceMonitorList;
import com.clustercontrol.utility.settings.monitor.xml.RpaManagementToolServiceMonitors;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.settings.monitor.xml.TruthValue;
import com.clustercontrol.utility.util.UtilityManagerUtil;

/**
 * RPA管理ツール監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 *
 */
public class RpaManagementToolServiceConv {
	private final static Log logger = LogFactory.getLog(RpaManagementToolServiceConv.class);

	private final static String SCHEMA_TYPE = "K";
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
	public static RpaManagementToolServiceMonitors createRpaManagementToolServiceMonitors(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		RpaManagementToolServiceMonitors rpaManagementToolServiceMonitors = new RpaManagementToolServiceMonitors();

		for (MonitorInfoResponse monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo =  MonitorsettingRestClientWrapper
					.getWrapper(UtilityManagerUtil.getCurrentManagerName())
					.getMonitor(monitorInfo.getMonitorId());

			RpaManagementToolServiceMonitor rpaManagementToolServiceMonitor = new RpaManagementToolServiceMonitor();
			rpaManagementToolServiceMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));

			for (MonitorTruthValueInfoResponse truthValueInfo : monitorInfo.getTruthValueInfo()) {
				rpaManagementToolServiceMonitor.addTruthValue(MonitorConv.createTruthValue(monitorInfo.getMonitorId(),truthValueInfo));
			}

			rpaManagementToolServiceMonitor.setRpaManagementToolServiceInfo(createRpaManagementToolServiceInfo(monitorInfo));
			rpaManagementToolServiceMonitors.addRpaManagementToolServiceMonitor(rpaManagementToolServiceMonitor);
		}

		rpaManagementToolServiceMonitors.setCommon(MonitorConv.versionDto2Xml());
		rpaManagementToolServiceMonitors.setSchemaInfo(getSchemaVersion());

		return rpaManagementToolServiceMonitors;
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
	public static List<MonitorInfoResponse> createMonitorInfoList(RpaManagementToolServiceMonitorList rpaManagementToolServiceMonitorList) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		List<MonitorInfoResponse> monitorInfoList = new LinkedList<MonitorInfoResponse>();

		for (RpaManagementToolServiceMonitor rpaManagementToolServiceMonitor : rpaManagementToolServiceMonitorList.getRpaManagementToolServiceMonitor()) {
			logger.debug("Monitor Id : " + rpaManagementToolServiceMonitor.getMonitor().getMonitorId());

			MonitorInfoResponse monitorInfo = MonitorConv.createMonitorInfo(rpaManagementToolServiceMonitor.getMonitor());
			for (TruthValue truthValue : rpaManagementToolServiceMonitor.getTruthValue()) {
				monitorInfo.getTruthValueInfo().add(MonitorConv.createTruthValue(truthValue));
			}

			monitorInfo.setRpaManagementToolServiceCheckInfo(createRpaManagementToolServiceCheckInfo(
					rpaManagementToolServiceMonitor.getRpaManagementToolServiceInfo()));
			monitorInfoList.add(monitorInfo);
		}

		return monitorInfoList;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static RpaManagementToolServiceInfo createRpaManagementToolServiceInfo(MonitorInfoResponse monitorInfo) {
		RpaManagementToolServiceInfo rpaManagementToolServiceInfo = new RpaManagementToolServiceInfo();
		rpaManagementToolServiceInfo.setMonitorTypeId("");
		rpaManagementToolServiceInfo.setMonitorId(monitorInfo.getMonitorId());
		
		if(monitorInfo.getRpaManagementToolServiceCheckInfo() != null){
			if(monitorInfo.getRpaManagementToolServiceCheckInfo().getConnectTimeout() != null){
				rpaManagementToolServiceInfo.setConnectTimeout(monitorInfo.getRpaManagementToolServiceCheckInfo().getConnectTimeout());
			}
			if(monitorInfo.getRpaManagementToolServiceCheckInfo().getRequestTimeout() != null){
				rpaManagementToolServiceInfo.setRequestTimeout(monitorInfo.getRpaManagementToolServiceCheckInfo().getRequestTimeout());
			}
		}

		return rpaManagementToolServiceInfo;
	}
	
	/**
	 * <BR>
	 *
	 * @return
	 */
	private static RpaManagementToolServiceCheckInfoResponse createRpaManagementToolServiceCheckInfo(RpaManagementToolServiceInfo rpaManagementToolServiceInfo) {
		RpaManagementToolServiceCheckInfoResponse rpaManagementToolServiceCheckInfo = new RpaManagementToolServiceCheckInfoResponse();
		if(rpaManagementToolServiceInfo.hasConnectTimeout()){
			rpaManagementToolServiceCheckInfo.setConnectTimeout(rpaManagementToolServiceInfo.getConnectTimeout());
		}
		if(rpaManagementToolServiceInfo.hasRequestTimeout()){
			rpaManagementToolServiceCheckInfo.setRequestTimeout(rpaManagementToolServiceInfo.getRequestTimeout());
		}

		return rpaManagementToolServiceCheckInfo;
	}
}