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

import javax.xml.ws.WebServiceException;

import com.clustercontrol.util.EndpointManager;
import com.clustercontrol.util.EndpointUnit.EndpointSetting;
import com.clustercontrol.utility.settings.ConvertorException;
import com.clustercontrol.utility.settings.model.BaseAction;
import com.clustercontrol.utility.settings.monitor.conv.JmxConv;
import com.clustercontrol.utility.settings.monitor.xml.JmxMonitor;
import com.clustercontrol.utility.settings.monitor.xml.JmxMonitors;
import com.clustercontrol.utility.util.UtilityManagerUtil;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;
import com.clustercontrol.ws.monitor.MonitorSettingEndpoint;
import com.clustercontrol.ws.monitor.MonitorSettingEndpointService;

/**
 * Jmx 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された Jmx 監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている Jmx 監視情報と重複する場合はスキップされる。
 *
 * @version 6.1.0
 * @since 5.0.a
 *
 */
public class JmxAction extends AbstractMonitorAction<JmxMonitors> {
	public JmxAction() throws ConvertorException {
		super();
	}

	@Override
	public Class<JmxMonitors> getDataClass() {
		return JmxMonitors.class;
	}

	@Override
	protected boolean checkSchemaVersionScope(JmxMonitors object) {
		int res = JmxConv.checkSchemaVersion(object.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				JmxConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	public List<MonitorInfo> createMonitorInfoList(JmxMonitors object) throws ConvertorException {
		return JmxConv.createMonitorInfoList(object);
	}

	@Override
	protected List<MonitorInfo> getFilterdMonitorList() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		return getEndpoint().getJmxList();
	}

	@Override
	protected JmxMonitors createCastorData(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		return JmxConv.createJmxMonitors(monitorInfoList);
	}

	@Override
	protected void sort(JmxMonitors object) {
		JmxMonitor[] ms = object.getJmxMonitor();
		Arrays.sort(
			ms,
			new Comparator<JmxMonitor>() {
				@Override
				public int compare(JmxMonitor obj1, JmxMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setJmxMonitor(ms);
	}

	public MonitorSettingEndpoint getEndpoint() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		WebServiceException wse = null;
		for (EndpointSetting<MonitorSettingEndpoint> endpointSetting : EndpointManager.get(UtilityManagerUtil.getCurrentManagerName()).getEndpoint(MonitorSettingEndpointService.class, MonitorSettingEndpoint.class)) {
			try {
				return endpointSetting.getEndpoint();
			} catch (WebServiceException e) {
				wse = e;
				getLogger().warn("getJmxList(), " + e.getMessage());
			}
		}
		throw wse;
	}
}
