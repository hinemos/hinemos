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
import com.clustercontrol.utility.settings.monitor.conv.PingConv;
import com.clustercontrol.utility.settings.monitor.xml.PingMonitor;
import com.clustercontrol.utility.settings.monitor.xml.PingMonitors;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;

/**
 * PING監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義されたPING監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されているPING監視情報と重複する場合はスキップされる。
 *
 * @version 6.1.0
 * @since 1.0.0
 *
 */
public class PingAction extends AbstractMonitorAction<PingMonitors> {
	public PingAction() throws ConvertorException {
		super();
	}

	@Override
	public Class<PingMonitors> getDataClass() {
		return PingMonitors.class;
	}

	@Override
	protected boolean checkSchemaVersionScope(PingMonitors object) {
		int res = PingConv.checkSchemaVersion(object.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				PingConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	public List<MonitorInfo> createMonitorInfoList(PingMonitors object) throws ConvertorException {
		return PingConv.createMonitorInfoList(object);
	}

	@Override
	protected List<MonitorInfo> getFilterdMonitorList() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		return MonitorSettingEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getPingList();
	}

	@Override
	protected PingMonitors createCastorData(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		return PingConv.createPingMonitors(monitorInfoList);
	}

	@Override
	protected void sort(PingMonitors object) {
		PingMonitor[] ms = object.getPingMonitor();
		Arrays.sort(
			ms,
			new Comparator<PingMonitor>() {
				@Override
				public int compare(PingMonitor obj1, PingMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setPingMonitor(ms);
	}
}
