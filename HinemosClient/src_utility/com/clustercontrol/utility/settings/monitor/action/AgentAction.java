/*
 * Copyright (c) 2018 NTT DATA INTELLILINK Corporation. All rights reserved.
 *
 * Hinemos (http://www.hinemos.info/)
 *
 * See the LICENSE file for licensing information.
 */

package com.clustercontrol.utility.settings.monitor.action;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.openapitools.client.model.AgentMonitorInfoResponse;
import org.openapitools.client.model.MonitorInfoResponse;

import com.clustercontrol.fault.HinemosUnknown;
import com.clustercontrol.fault.InvalidRole;
import com.clustercontrol.fault.InvalidSetting;
import com.clustercontrol.fault.InvalidUserPass;
import com.clustercontrol.fault.MonitorNotFound;
import com.clustercontrol.fault.RestConnectFailed;
import com.clustercontrol.monitor.util.MonitorsettingRestClientWrapper;
import com.clustercontrol.util.Messages;
import com.clustercontrol.util.RestClientBeanUtil;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.monitor.conv.AgentConv;
import com.clustercontrol.utility.settings.monitor.xml.AgentMonitor;
import com.clustercontrol.utility.settings.monitor.xml.AgentMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;

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
		this.targetTitle = Messages.getString("monitor.agent");
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
	protected List<MonitorInfoResponse> createMonitorInfoList(AgentMonitors object) throws ConvertorException, InvalidSetting, HinemosUnknown, ParseException {
		return AgentConv.createMonitorInfoList(object);
	}

	@Override
	protected List<MonitorInfoResponse> getFilterdMonitorList()
			throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed {
		List<AgentMonitorInfoResponse> resDtoList = MonitorsettingRestClientWrapper.getWrapper(UtilityManagerUtil.getCurrentManagerName()).getAgentList(null);
		List<MonitorInfoResponse> retList = new ArrayList<MonitorInfoResponse>();
		for(AgentMonitorInfoResponse recSrc :resDtoList){
			MonitorInfoResponse recDest = new MonitorInfoResponse();
			RestClientBeanUtil.convertBean(recSrc, recDest);
			retList.add(recDest);
		}
		return retList;
	}

	@Override
	protected AgentMonitors createCastorData(
			List<MonitorInfoResponse> monitorInfoList) throws HinemosUnknown, InvalidRole, InvalidUserPass, MonitorNotFound, RestConnectFailed, ParseException {
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