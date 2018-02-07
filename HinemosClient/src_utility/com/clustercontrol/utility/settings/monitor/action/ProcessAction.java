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
import com.clustercontrol.utility.settings.monitor.conv.ProcessConv;
import com.clustercontrol.utility.settings.monitor.xml.ProcessMonitor;
import com.clustercontrol.utility.settings.monitor.xml.ProcessMonitors;
import com.clustercontrol.ws.monitor.HinemosUnknown_Exception;
import com.clustercontrol.ws.monitor.InvalidRole_Exception;
import com.clustercontrol.ws.monitor.InvalidUserPass_Exception;
import com.clustercontrol.ws.monitor.MonitorInfo;
import com.clustercontrol.ws.monitor.MonitorNotFound_Exception;

/**
 * PROCESS監視設定情報を取得、設定、削除します。<br>
 * XMLファイルに定義されたPROCESS監視情報をマネージャに反映させるクラス<br>
 * ただし、すでに登録されているPROCESS監視情報と重複する場合はスキップされる。
 *
 * @version 6.1.0
 * @since 1.0.0
 *
 */
public class ProcessAction extends AbstractMonitorAction<ProcessMonitors> {
	public ProcessAction() throws ConvertorException {
		super();
	}

	@Override
	protected boolean checkSchemaVersionScope(ProcessMonitors object) {
		int res = ProcessConv.checkSchemaVersion(object.getSchemaInfo());
		com.clustercontrol.utility.settings.monitor.xml.SchemaInfo sci = 
				ProcessConv.getSchemaVersion();
		return BaseAction.checkSchemaVersionResult(this.getLogger(), res, sci.getSchemaType(), sci.getSchemaVersion(), sci.getSchemaRevision());
	}

	@Override
	protected Class<ProcessMonitors> getDataClass() {
		return ProcessMonitors.class;
	}

	@Override
	protected List<MonitorInfo> createMonitorInfoList(ProcessMonitors object) throws ConvertorException {
		return ProcessConv.createMonitorInfoList(object);
	}

	@Override
	protected List<MonitorInfo> getFilterdMonitorList()
			throws HinemosUnknown_Exception, InvalidRole_Exception,
			InvalidUserPass_Exception, MonitorNotFound_Exception {
		return MonitorSettingEndpointWrapper.getWrapper(ClusterControlPlugin.getDefault().getCurrentManagerName()).getProcessList();
	}

	@Override
	protected ProcessMonitors createCastorData(List<MonitorInfo> monitorInfoList) throws HinemosUnknown_Exception, InvalidRole_Exception, InvalidUserPass_Exception, MonitorNotFound_Exception {
		return ProcessConv.createProcessMonitors(monitorInfoList);
	}

	@Override
	protected void sort(ProcessMonitors object) {
		ProcessMonitor[] ms = object.getProcessMonitor();
		Arrays.sort(
			ms,
			new Comparator<ProcessMonitor>() {
				@Override
				public int compare(ProcessMonitor obj1, ProcessMonitor obj2) {
					return obj1.getMonitor().getMonitorId().compareTo(obj2.getMonitor().getMonitorId());
				}
			});
		 object.setProcessMonitor(ms);
	}
}
