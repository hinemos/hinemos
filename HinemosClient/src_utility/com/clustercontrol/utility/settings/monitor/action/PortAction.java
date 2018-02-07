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
import com.clustercontrol.utility.settings.monitor.conv.PortConv;
import com.clustercontrol.utility.settings.monitor.xml.PortMonitor;
import com.clustercontrol.utility.settings.monitor.xml.PortMonitors;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;

/**
 * ポート 監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義された ポート 監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されている ポート 監視情報と重複する場合はスキップされる。
 *
 * @version 6.1.0
 * @since 1.0.0
 *
 */
public class PortAction extends AbstractMonitorAction<PortMonitors> {
	public PortAction() throws ConvertorException {
		super();
	}

	@Override
	public Class<PortMonitors> getDataClass() {
		return PortMonitors.class;
	}

	@Override
	protected boolean checkSchemaVersionScope(PortMonitors object) {
		int res = PortConv.checkSchemaVersion(object.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				PortConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	public List<MonitorInfo> createMonitorInfoList(PortMonitors object) throws ConvertorException {
		return PortConv.createMonitorInfoList(object);
	}

	@Override
	protected List<MonitorInfo> getFilterdMonitorList() throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		return MonitorSettingEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getPortList();
	}

	@Override
	protected PortMonitors createCastorData(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		return PortConv.createPortMonitors(monitorInfoList);
	}

	@Override
	protected void sort(PortMonitors object) {
		PortMonitor[] ms = object.getPortMonitor();
		Arrays.sort(
			ms,
			new Comparator<PortMonitor>() {
				@Override
				public int compare(PortMonitor obj1, PortMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setPortMonitor(ms);
	}
}