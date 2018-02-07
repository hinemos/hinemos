/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.action;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.clustercontrol.ClusterControlPlugin;
import com.clustercontrol.monitor.util.MonitorSettingEndpointWrapper;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.monitor.conv.AgentConv;
import com.clustercontrol.utility.settings.monitor.xml.AgentMonitor;
import com.clustercontrol.utility.settings.monitor.xml.AgentMonitors;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;

/**
 * AGENT監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義されたAGENT監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されているAGENT監視情報と重複する場合はスキップされる。
 *
 * @version 6.1.0
 * @since 1.0.0
 *
 *
 */
public class AgentAction extends AbstractMonitorAction<AgentMonitors> {

	public AgentAction() throws ConvertorException {
		super();
	}

	@Override
	protected Class<AgentMonitors> getDataClass() {
		return AgentMonitors.class;
	}

	@Override
	protected boolean checkSchemaVersionScope(AgentMonitors object) {
		int res = AgentConv.checkSchemaVersion(object.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				AgentConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	protected List<MonitorInfo> createMonitorInfoList(AgentMonitors object) throws ConvertorException {
		return AgentConv.createMonitorInfoList(object);
	}

	@Override
	protected List<MonitorInfo> getFilterdMonitorList()
			throws com.clustercontrol.ws.monitor.HinemosUnknown_Exception,
			com.clustercontrol.ws.monitor.InvalidRole_Exception,
			com.clustercontrol.ws.monitor.InvalidUserPass_Exception,
			MonitorNotFound_Exception {
		return MonitorSettingEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getAgentList();
	}

	@Override
	protected AgentMonitors createCastorData(
			List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		return AgentConv.createAgentMonitors(monitorInfoList);
	}

	@Override
	protected void sort(AgentMonitors object) {
		AgentMonitor[] ams = object.getAgentMonitor();
		Arrays.sort(
			ams,
			new Comparator<AgentMonitor>() {
				@Override
				public int compare(AgentMonitor obj1, AgentMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setAgentMonitor(ams);
	}
}