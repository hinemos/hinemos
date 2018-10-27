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

import com.clustercontrol.bean.HinemosModuleConstant;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseConv;
import com.clustercontrol.utility.settings.monitor.xml.AgentInfo;
import com.clustercontrol.utility.settings.monitor.xml.AgentMonitor;
import com.clustercontrol.utility.settings.monitor.xml.AgentMonitorList;
import com.clustercontrol.utility.settings.monitor.xml.AgentMonitors;
import com.clustercontrol.utility.settings.monitor.xml.SchemaInfo;
import com.clustercontrol.utility.settings.monitor.xml.TruthValue;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;
import com.clustercontrol.ws.monitor.MonitorTruthValueInfo;

/**
 * AGENT 監視設定情報を Castor のデータ構造と DTO との間で相互変換するクラス<BR>
 *
 * @version 6.1.0
 * @since 1.0.0
 *
 */
public class AgentConv {
	private final static Log logger = LogFactory.getLog(AgentConv.class);

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
	public static AgentMonitors createAgentMonitors(List<com.clustercontrol.ws.monitor.MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		AgentMonitors agentMonitorList = new AgentMonitors();

		for (MonitorInfo monitorInfo : monitorInfoList) {
			logger.debug("Monitor Id : " + monitorInfo.getMonitorId());

			monitorInfo =  MonitorSettingEndpointWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getMonitor(monitorInfo.getMonitorId());

			AgentMonitor agentMonitor = new AgentMonitor();
			agentMonitor.setMonitor(MonitorConv.createMonitor(monitorInfo));

			for (MonitorTruthValueInfo truthValueInfo : monitorInfo.getTruthValueInfo()) {
				agentMonitor.addTruthValue(MonitorConv.createTruthValue(truthValueInfo));
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
	 */
	public static List<MonitorInfo> createMonitorInfoList(AgentMonitorList agentMonitorList) throws ConvertorException {
		List<MonitorInfo> monitorInfoList = new LinkedList<MonitorInfo>();

		for (AgentMonitor agentMonitor : agentMonitorList.getAgentMonitor()) {
			logger.debug("Monitor Id : " + agentMonitor.getMonitor().getMonitorId());

			MonitorInfo monitorInfo = MonitorConv.createMonitorInfo(agentMonitor.getMonitor());
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
	private static AgentInfo createAgentInfo(MonitorInfo monitorInfo) {
		assert HinemosModuleConstant.MONITOR_AGENT.equals(monitorInfo.getMonitorTypeId()): "monitorInfo.getMonitorTypeId() == HinemosModuleConstant.TYPE_MONITOR_AGENT";

		AgentInfo agentInfo = new AgentInfo();
		agentInfo.setMonitorTypeId("");
		agentInfo.setMonitorId(monitorInfo.getMonitorId());

		return agentInfo;
	}
}