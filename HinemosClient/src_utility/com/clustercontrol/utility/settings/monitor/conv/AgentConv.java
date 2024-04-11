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

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.monitor.xml.AgentInfo;
import com.clustercontrol.utility.settings.monitor.xml.AgentMonitor;
import com.clustercontrol.utility.settings.monitor.xml.AgentMonitorList;
import com.clustercontrol.utility.settings.monitor.xml.AgentMonitors;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.settings.monitor.xml.TruthValue;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.version.util.VersionUtil;

/**
 * AGENT 監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 *
 * @version 6.1.0
 * @since 1.0.0
 *
 */
public class AgentConv {
	private final static Log logger = LogFactory.getLog(AgentConv.class);

	/**
	 * 同一バイナリ化対応により、スキーマ情報はHinemosVersion.jarのVersionUtilクラスから取得されることになった。
	 * スキーマ情報の一覧はhinemos_version.properties.implに記載されている。
	 * スキーマ情報に変更がある場合は、まずbuild_common_version.properties.implを修正し、
	 * 対象のスキーマ情報が初回の修正であるならばhinemos_version.properties.implも修正する。
	 */
	static private String SCHEMA_TYPE = VersionUtil.getSchemaProperty("MONITOR.AGENT.SCHEMATYPE");
	static private String SCHEMA_VERSION = VersionUtil.getSchemaProperty("MONITOR.AGENT.SCHEMAVERSION");
	static private String SCHEMA_REVISION =VersionUtil.getSchemaProperty("MONITOR.AGENT.SCHEMAREVISION");

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
	public static AgentMonitors createAgentMonitors(List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
		AgentMonitors agentMonitorList = new AgentMonitors();

		for (MonitorInfoResponse monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo =  MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getMonitor(monitorInfo.getMonitorId());

			AgentMonitor agentMonitor = new AgentMonitor();
			agentMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));

			for (MonitorTruthValueInfoResponse truthValueInfo : monitorInfo.getTruthValueInfo()) {
				agentMonitor.addTruthValue(MonitorConv.createTruthValue(monitorInfo.getMonitorId(),truthValueInfo));
			}

			agentMonitor.setAgentInfo(createAgentInfo(monitorInfo));
			agentMonitorList.addAgentMonitor(agentMonitor);
		}

		agentMonitorList.setCommon(MonitorConv.versionDto2Xml());
		agentMonitorList.setSchemaInfo(getSchemaVersion());

		return agentMonitorList;
	}

	/**
	 * <BR>
	 *
	 * @return
	 * @throws ParseException 
	 * @throws HinemosUnknown 
	 * @throws InvalidSetting 
	 */
	public static List<MonitorInfoResponse> createMonitorInfoList(AgentMonitorList agentMonitorList) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		List<MonitorInfoResponse> monitorInfoList = new LinkedList<MonitorInfoResponse>();

		for (AgentMonitor agentMonitor : agentMonitorList.getAgentMonitor()) {
			logger.debug("Monitor Id : " + agentMonitor.getMonitor().getMonitorId());

			MonitorInfoResponse monitorInfo = MonitorConv.createMonitorInfo(agentMonitor.getMonitor());
			for (TruthValue truthValue : agentMonitor.getTruthValue()) {
				monitorInfo.getTruthValueInfo().add(MonitorConv.createTruthValue(truthValue));
			}

			monitorInfoList.add(monitorInfo);
		}

		return monitorInfoList;
	}

	/**
	 * <BR>
	 *
	 * @return
	 */
	private static AgentInfo createAgentInfo(MonitorInfoResponse monitorInfo) {
		assert HinemosModuleConstant.MONITOR_AGENT.equals(monitorInfo.getMonitorTypeId()): "monitorInfo.getMonitorTypeId() == HinemosModuleConstant.TYPE_MONITOR_AGENT";

		AgentInfo agentInfo = new AgentInfo();
		agentInfo.setMonitorTypeId("");
		agentInfo.setMonitorId(monitorInfo.getMonitorId());

		return agentInfo;
	}
}